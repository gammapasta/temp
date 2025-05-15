package com.team109.javara.global.sse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.team109.javara.domain.tracking.dto.TrackingDecisionResponseDto;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SseEmitterService sseEmitterService;


    public void notifyForTaskDecision(Long memberId, String wantedVehicleNumber ,Long taskId){
        TrackingDecisionResponseDto responseDto = new TrackingDecisionResponseDto();

        responseDto.setMemberId(memberId);
        responseDto.setWantedVehicleNumber(wantedVehicleNumber);
        responseDto.setTaskId(taskId);

        log.info("SSE 알림 보내기 시작: member [{}]", memberId);
        try {
            String eventName = "decision-alert";
            sseEmitterService.sendEventToPolice(memberId, eventName, responseDto);

            log.info("SSE 이벤트 [{}] 알림 보내기 성공: member [{}]", eventName, memberId);

        } catch (Exception e) {
            log.error("SSE 알림 보내기 실패: member [{}]. 상세내용 [{}]", memberId, e.getMessage());

        }
    }

}
