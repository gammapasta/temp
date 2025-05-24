package com.team109.javara.global.common.scheduler;

import com.team109.javara.domain.event.event.EdgeDeviceEvent;
import com.team109.javara.domain.event.event.ServerInitiatedEvent;
import com.team109.javara.domain.event.service.AsyncServerInitiatedEvent;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.location.repository.WantedVehicleLocationRepository;
import com.team109.javara.domain.location.service.PoliceFindService;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.task.entity.enums.TaskStatus;
import com.team109.javara.domain.task.repository.TaskRepository;
import com.team109.javara.domain.vehicle.component.WantedSet;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServerInitiatedTaskAssignmentScheduler {
    private final WantedSet wantedSet;
    private final ApplicationEventPublisher eventPublisher;
    private final WantedVehicleLocationRepository wantedVehicleLocationRepository;
    private final PoliceFindService policeFindService;
    private final AsyncServerInitiatedEvent asyncServerInitiatedEvent;

    @Scheduled(fixedDelay = 1000 * 10) // 이전작업 끝난 후 10초마다
    public void ServerInitiatedTaskAssignmentSchedule(){

        // 일반 시민이 찾은 수배차량이 1개 이상이라면
        if(wantedSet.size() > 0){
            log.info("서버주도임무부여 스케줄러 수배차량 개수 {}", wantedSet.size());
            List<String> wantedVehicleList = wantedSet.getWantedListFromSet();
            for(String wantedVehicleNumber : wantedVehicleList){

                // 가까운 경찰 찾기
                Member member = policeFindService.findAvailablePoliceFromServerInitiatedTaskAssignmentSchedule(wantedVehicleNumber);
                if(member == null){
                    wantedSet.remove(wantedVehicleNumber);
                    break;
                }
                log.info("서버주도임무부여 async 알림 보냄");
                //eventPublisher.publishEvent(new ServerInitiatedEvent(member.getId(), wantedVehicleNumber));
                asyncServerInitiatedEvent.serverInitiatedTaskAssignment(member.getId(), wantedVehicleNumber);
                log.info("서버주도임무부여 async 알림 보내기 성공");
            }
        }else {
            log.info("서버주도임무부여 스케줄러 수배차량 개수 {}", wantedSet.size());
        }







    }

}
