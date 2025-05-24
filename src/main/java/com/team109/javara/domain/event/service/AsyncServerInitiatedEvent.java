package com.team109.javara.domain.event.service;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.task.entity.enums.TaskStatus;
import com.team109.javara.domain.task.repository.TaskRepository;
import com.team109.javara.domain.vehicle.component.WantedSet;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import com.team109.javara.global.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncServerInitiatedEvent {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final WantedSet wantedSet;

    // 서버 주도 임무 부여
    @Transactional
    public void serverInitiatedTaskAssignment(Long memberId, String wantedVehicleNumber) {
        log.info("[Async] 서버 주도 임무 부여 시작 [{}] [{}]", memberId, wantedVehicleNumber);
        Task task = taskRepository.findByWantedVehicle_WantedVehicleNumber(wantedVehicleNumber).orElseThrow(() -> new GlobalException(ErrorCode.TASK_NOT_FOUND));
        if (task.getTaskStatus() == TaskStatus.ACTIVE || task.getTaskStatus() == TaskStatus.FAILED) {
            Member firstPoliceToAssign = memberRepository.findById(memberId).orElseThrow(()-> new GlobalException(ErrorCode.MEMBER_NOT_FOUND, "엣지디바이스에 연결된 사용자를 못 찾았어요."));

            task.setTaskStatus(TaskStatus.ASSIGNED);
            task.setAssignedMember(firstPoliceToAssign);
            taskRepository.save(task);

            //  경찰 앱으로 수락여부알림보냄 -> 현재는 경찰이 active 상태라 policeLocation에 저장중임, 수락 거절 후 wantedVehicleLocation에 저장됨, 하지만 일반인은 다름
            notificationService.notifyForTaskDecision(firstPoliceToAssign.getId(), wantedVehicleNumber, task.getTaskId());
            log.info("Task [{}] 상태 [ASSIGNED], 경찰한테 알림 보내기 성공 member [{}]", task.getTaskId(), firstPoliceToAssign);

            if(wantedSet.contains(wantedVehicleNumber)){
                wantedSet.remove(wantedVehicleNumber);
            }

        }else {

            task.setTaskStatus(TaskStatus.ACTIVE);
            task.setAssignedMember(null);

            taskRepository.save(task);
            log.warn("task [{}]가 [active] 아닙니다, 다시 바꿈 [{}]", task.getTaskId(), task.getTaskStatus());
        }
    }
}
