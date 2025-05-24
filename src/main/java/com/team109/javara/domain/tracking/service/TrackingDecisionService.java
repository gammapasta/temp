package com.team109.javara.domain.tracking.service;




import com.team109.javara.domain.event.service.AsyncDecisionService;
import com.team109.javara.domain.location.service.PoliceFindService;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.event.event.TaskEvent;
import com.team109.javara.domain.task.entity.enums.TaskStatus;
import com.team109.javara.domain.task.repository.TaskRepository;
import com.team109.javara.domain.task.service.TaskService;
import com.team109.javara.domain.tracking.dto.TrackingDecisionRequestDto;
import com.team109.javara.domain.tracking.dto.TrackingResultRequestDto;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import com.team109.javara.global.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingDecisionService {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final TaskService taskService;
    private final WantedVehicleRepository wantedVehicleRepository;
    private final MemberService memberService;
    private final PoliceFindService policeFindService;
    private final AsyncDecisionService asyncDecisionService;
    private final ApplicationEventPublisher eventPublisher; //트랜잭션 끝나고 나서 이밴트 보내야함
    //엣지디바이스<->서버
    //경찰이 active 일때 수배차량 발견하고 최초 할당 시
    @Transactional
    public void initiateFirstTaskDecision(String firstRespondingDeviceId, String wantedVehicleNumber) {
        log.info("initiateFirstTaskDecision 시작 [{}] [{}]", firstRespondingDeviceId, wantedVehicleNumber);
        Task task = taskRepository.findByWantedVehicle_WantedVehicleNumber(wantedVehicleNumber).orElseThrow(() -> new GlobalException(ErrorCode.TASK_NOT_FOUND));
        if (task.getTaskStatus() == TaskStatus.ACTIVE) {
            Member firstPoliceToAssign = memberRepository.findByEdgeDeviceId(firstRespondingDeviceId).orElseThrow(()-> new GlobalException(ErrorCode.MEMBER_NOT_FOUND, "엣지디바이스에 연결된 사용자를 못 찾았어요."));

            task.setTaskStatus(TaskStatus.ASSIGNED);
            task.setAssignedMember(firstPoliceToAssign);
            taskRepository.save(task);

            //  경찰 앱으로 수락여부알림보냄 -> 현재는 경찰이 active 상태라 policeLocation에 저장중임, 수락 거절 후 wantedVehicleLocation에 저장됨, 하지만 일반인은 다름
            notificationService.notifyForTaskDecision(firstPoliceToAssign.getId(), wantedVehicleNumber, task.getTaskId());
            log.info("Task [{}] 상태 [ASSIGNED], 경찰한테 알림 보내기 성공 member [{}]", task.getTaskId(), firstPoliceToAssign);
        }else {
            log.warn("task [{}]가 [active] 아닙니다", task.getTaskId());
            throw new RuntimeException();
        }
    }

    // 앱 <-> 서버
    @Transactional
    public void handleTrackingDecision(TrackingDecisionRequestDto requestDto) {
        // 1. tWantedVehicleNumber에 연관된 task 찾기
        Task task = taskRepository.findByWantedVehicle_WantedVehicleNumber(requestDto.getWantedVehicleNumber())
                .orElseThrow(() -> new GlobalException(ErrorCode.TASK_NOT_FOUND));
        WantedVehicle wantedVehicle = wantedVehicleRepository.findByWantedVehicleNumber(requestDto.getWantedVehicleNumber())
                .orElseThrow(() -> new GlobalException(ErrorCode.VEHICLE_NOT_FOUND));


        Long memberId = requestDto.getMemberId();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 Task가 할당된 것인지 확인.
        // ASSIGNED 상태는 모바일 앱으로 알림을 보낸 상태이다
        if (task.getTaskStatus() == TaskStatus.ASSIGNED) {
            if (task.getAssignedMember() == null || !task.getAssignedMember().getId().equals(member.getId())) {
                // 이 Task는 현재 이 경찰에게 할당된 것이 아님.
                throw new GlobalException(ErrorCode.TASK_NOT_ASSIGNED_TO_THIS_police);
            }
        }

        // ACCEPTED 상태인데 다른 경찰이 또 수락하려고 하는 경우 방지
        else if (task.getTaskStatus() == TaskStatus.ACCEPTED) {
            if (task.getAssignedMember() == null || !task.getAssignedMember().getId().equals(member.getId())) {
                // 이미 다른 경찰이 수락한 임무
                throw new GlobalException(ErrorCode.TASK_ALREADY_ACCEPTED_BY_OTHER);
            }
            // 이미  수락한 임무에 또 수락 요청이 온 경우 무시
            log.warn("Task [{}]는 member [{}]가 이미  [ACCEPTED] 했습니다", task.getTaskId(), memberId);
            throw new GlobalException(ErrorCode.TASK_ALREADY_ACCEPTED);

        }


        //ACCEPTED
        if (requestDto.getDecision() == TrackingDecisionRequestDto.Decision.ACCEPTED) {
            // 수락은 ASSIGNED 상태에서만 가능
            if (task.getTaskStatus() == TaskStatus.ASSIGNED) {
                task.setTaskStatus(TaskStatus.ACCEPTED);
                task.setAssignedMember(member); // 처음 발견한 사람을 task의 맴버id로 설정
                task.setUpdatedAt(LocalDateTime.now()); //날짜 업데이트
                taskRepository.save(task);

                //수배차량 추적중
                wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.TRACKING);
                wantedVehicle.setUpdatedAt(LocalDateTime.now());
                wantedVehicleRepository.save(wantedVehicle);

                // member 상태 TRACKING
                memberService.updateMemberStatus(member, MemberStatus.TRACKING);

                log.info("Task [{}] [ACCEPTED]: member [{}]", task.getTaskId(), memberId);
            } else {
                throw new GlobalException(ErrorCode.INVALID_TASK_STATUS_FOR_ACCEPTANCE);
            }

        } else { // REJECTED
            // 거절은 ASSIGNED 상태에서만 가능
            if (task.getTaskStatus() == TaskStatus.ASSIGNED) {
                task.setTaskStatus(TaskStatus.REJECTED);
                task.setAssignedMember(null);
                task.setUpdatedAt(LocalDateTime.now());
                taskRepository.save(task);

                //수배차량 수배중
                wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.WANTED);
                wantedVehicle.setUpdatedAt(LocalDateTime.now());
                wantedVehicleRepository.save(wantedVehicle);

                // member 상태 NOT_AVAILABLE
                memberService.updateMemberStatus(member, MemberStatus.NOT_AVAILABLE);

                log.info("Task [{}] [REJECTED]: member [{}]. 다른 경찰관 찾기 시작", task.getTaskId(), memberId);

                eventPublisher.publishEvent(new TaskEvent(task.getTaskId(), wantedVehicle.getWantedVehicleId()));

            } else {
                throw new GlobalException(ErrorCode.INVALID_TASK_STATUS_FOR_REJECTION);
            }
        }


    }

    // 거절 후 다음 경찰에게 할당 시
    // [서버 주도 임무 할당]

//    public void findAnotherPolice(Task task) {
//        Member nextPolice = policeFindService.findNextAvailablePolice(task);
//
//        WantedVehicle wantedVehicle = wantedVehicleRepository.findByWantedVehicleNumber(task.getWantedVehicle().getWantedVehicleNumber())
//                .orElseThrow(() -> new GlobalException(ErrorCode.VEHICLE_NOT_FOUND));
//
//        // 찾으면
//        if (nextPolice != null) {
//            task.setTaskStatus(TaskStatus.ASSIGNED);
//            task.setAssignedMember(nextPolice); // 다음 경찰
//            task.setUpdatedAt(LocalDateTime.now());
//            taskRepository.save(task);
//
//            //수배차량 추적중
//            wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.TRACKING);
//            wantedVehicle.setUpdatedAt(LocalDateTime.now());
//            wantedVehicleRepository.save(wantedVehicle);
//
//            memberService.updateMemberStatus(nextPolice, MemberStatus.TRACKING);
//
//            notificationService.notifyForTaskDecision(nextPolice.getId(), task.getWantedVehicle().getWantedVehicleNumber() ,task.getTaskId());
//            log.info("Task [{}] [ASSIGNED] -> new Police [{}].", task.getTaskId(), nextPolice.getId());
//        } else {
//            task.setTaskStatus(TaskStatus.FAILED);
//            task.setAssignedMember(null);
//            task.setUpdatedAt(LocalDateTime.now());
//            // failed 처리 하는 이유 -> 일단 현재 추적이 불가능 -> 다음번에
//
//            //수배차량 추적중
//            wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.WANTED);
//            wantedVehicle.setUpdatedAt(LocalDateTime.now());
//            wantedVehicleRepository.save(wantedVehicle);
//
//            taskRepository.save(task);
//            log.warn("Task [{}] [REJECTED], Task [FAILED].", task.getTaskId());
//        }
//    }


    @Transactional
    public void handleResultDecision(TrackingResultRequestDto requestDto){
        Task task = taskRepository.findById(requestDto.getTaskId())
                .orElseThrow(() -> new GlobalException(ErrorCode.TASK_NOT_FOUND));
        WantedVehicle wantedVehicle = wantedVehicleRepository.findByWantedVehicleNumber(requestDto.getWantedVehicleNumber())
                .orElseThrow(() -> new GlobalException(ErrorCode.VEHICLE_NOT_FOUND));


        Long reportingPoliceId = requestDto.getMemberId();
        log.info("handleResultDecision시작 {} {} {} {}",reportingPoliceId,task.getTaskId(),wantedVehicle.getWantedVehicleId(), task.getTaskStatus());

        // 1. Task가 ACCEPTED 상태 검증
        if (task.getTaskStatus() != TaskStatus.ACCEPTED) {
            log.warn("Task [{}] [ACCEPTED] 상태 아닙니다. (현재: {}) 임무 결과 설정 불가능", task.getTaskId(), task.getTaskStatus());
            throw new GlobalException(ErrorCode.INVALID_TASK_STATUS_FOR_RESULT_PROCESSING);
        }
        // 2. Task를 할당받은 경찰이 보고한 경찰과 일치?
        if (task.getAssignedMember() == null || !task.getAssignedMember().getId().equals(reportingPoliceId)) {
            log.warn("Task [{}] 결과가 경찰 [{}]로부터 결과가 왔지만 다른 경찰한테 할당되어있습니다: 할당된 경찰 [{}].",
                    task.getTaskId(), reportingPoliceId, task.getAssignedMember() != null ? task.getAssignedMember().getId() : "null");
            throw new GlobalException(ErrorCode.TASK_NOT_ASSIGNED_TO_THIS_police_FOR_EXECUTION,"해당 임무 결과가 다른 경찰에 할당되어 있어 완료 할 수 없습니다. Task:"+task.getTaskId()+" 할당된 경찰: " +task.getAssignedMember().getId());
        }

        Member CurrentPolice = task.getAssignedMember();
        log.info("result       wanted {}", wantedVehicle.getWantedVehicleId());
        // 임무 완료 COMPLETE
        if (requestDto.getDecision() == TrackingResultRequestDto.Decision.COMPLETED) {
            //임무 완료
            taskService.taskCompleted(task);

            //수배차량 잡힘
            wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.CAPTURED);
            wantedVehicle.setUpdatedAt(LocalDateTime.now());
            wantedVehicleRepository.save(wantedVehicle);

            // 완료한 경찰의 상태를 ACTIVE로 변경
            memberService.updateMemberStatus(CurrentPolice, MemberStatus.ACTIVE);

            log.info("Task [{}] COMPLETED: member [{}].", task.getTaskId(), CurrentPolice.getId());

            //어드민 웹에 알림 가능

        }
        // 임무 수행 실패 FAIL
        else if (requestDto.getDecision() == TrackingResultRequestDto.Decision.FAILED) {

            log.info("Task [{}] FAILED: member [{}].", task.getTaskId(), CurrentPolice.getId());

            task.setTaskStatus(TaskStatus.FAILED);
            task.setAssignedMember(null);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            //수배차량 다시 wanted
            wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.WANTED);
            wantedVehicle.setUpdatedAt(LocalDateTime.now());
            wantedVehicleRepository.save(wantedVehicle);

            // 싶패한 경찰의 상태를 ACTIVE로 변경
            memberService.updateMemberStatus(CurrentPolice, MemberStatus.ACTIVE);


            eventPublisher.publishEvent(new TaskEvent(task.getTaskId(), wantedVehicle.getWantedVehicleId()));
            //asyncDecisionService.findAnotherPolice(task);


//            //밑에 중복되는거 바꿔야핟듯
//            Member nextPolice =  task.getAssignedMember(); //policeFinderService.findNextAvailablepolice(task, currentPolice);
//
//
//            // 다음 경찰을 찾으면
//            if (nextPolice != null) {
//                task.setTaskStatus(TaskStatus.ASSIGNED);
//                task.setAssignedMember(nextPolice);
//                task.setUpdatedAt(LocalDateTime.now());
//                taskRepository.save(task);
//                //수배차량 추적중
//                wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.TRACKING);
//                wantedVehicle.setUpdatedAt(LocalDateTime.now());
//                wantedVehicleRepository.save(wantedVehicle);
//
//                memberService.updateMemberStatus(nextPolice, MemberStatus.TRACKING);
//
//                String wantedVehicleNumber = task.getWantedVehicle().getWantedVehicleNumber();
//                // 새로운 경찰에게 알림 보내기 (NotificationService 사용)
//                notificationService.notifyForTaskDecision(nextPolice.getId(), wantedVehicleNumber, task.getTaskId());
//                log.info("Task [{}] 다른 경찰 [{}] 에게 재할당 알림 보냄.", task.getTaskId(), nextPolice.getId());
//
//            } else {
//                // 재할당할 다음 경찰을 못찾으면 fail
//                task.setTaskStatus(TaskStatus.FAILED);
//                task.setAssignedMember(null);
//                task.setUpdatedAt(LocalDateTime.now());
//                taskRepository.save(task);
//                //수배차량 잡힘
//                wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.WANTED);
//                wantedVehicle.setUpdatedAt(LocalDateTime.now());
//                wantedVehicleRepository.save(wantedVehicle);
//
//                log.warn("Task [{}] 경찰 [{}]이 reject 후 다른 경찰 못찾았다.", task.getTaskId(), CurrentPolice.getId());
//            }


           // memberService.updateMemberStatus(CurrentPolice, MemberStatus.ACTIVE);

        } else {
            log.warn("뭔가 오류 {} ",requestDto.getDecision());
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "Decision값은 enum만 가능합니다.");
        }
    }




}