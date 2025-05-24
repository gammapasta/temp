package com.team109.javara.domain.event.service;

import com.team109.javara.domain.location.service.PoliceFindService;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.task.entity.enums.TaskStatus;
import com.team109.javara.domain.task.repository.TaskRepository;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import com.team109.javara.global.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncDecisionService {
    private final TaskRepository taskRepository;
    private final WantedVehicleRepository wantedVehicleRepository;
    private final PoliceFindService policeFindService;
    private final MemberService memberService;
    private final NotificationService notificationService;

    public void findAnotherPolice(Long taskId, Long WantedVehicleId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new GlobalException(ErrorCode.TASK_NOT_FOUND));

        log.info("다음경찰찾기 시작");
        Member nextPolice = policeFindService.findNextAvailablePolice(task);

        // 기존 task로 찾으려면 안되는 이유가 manytoone lazy라서 초기화가 안됨
        WantedVehicle wantedVehicle = wantedVehicleRepository.findById(WantedVehicleId)
                .orElseThrow(() -> new GlobalException(ErrorCode.VEHICLE_NOT_FOUND));
        log.info("[Async] findAnotherPolice");

        // 찾으면
        if (nextPolice != null) {
            task.setTaskStatus(TaskStatus.ASSIGNED);
            task.setAssignedMember(nextPolice); // 다음 경찰
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            //수배차량 추적중
            wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.TRACKING);
            wantedVehicle.setUpdatedAt(LocalDateTime.now());
            wantedVehicleRepository.save(wantedVehicle);

            memberService.updateMemberStatus(nextPolice, MemberStatus.TRACKING);

            notificationService.notifyForTaskDecision(nextPolice.getId(), task.getWantedVehicle().getWantedVehicleNumber() ,task.getTaskId());
            log.info("[Async] Task [{}] [ASSIGNED] -> new Police [{}].", task.getTaskId(), nextPolice.getId());
        } else {
            task.setTaskStatus(TaskStatus.FAILED);
            task.setAssignedMember(null);
            task.setUpdatedAt(LocalDateTime.now());
            // failed 처리 하는 이유 -> 일단 현재 추적이 불가능 -> 다음번에

            //수배차량 추적중
            wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.WANTED);
            wantedVehicle.setUpdatedAt(LocalDateTime.now());
            wantedVehicleRepository.save(wantedVehicle);

            taskRepository.save(task);
            log.warn("[Async] Task [{}] [REJECTED], Task [FAILED].", task.getTaskId());
        }
    }

}
