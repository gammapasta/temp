package com.team109.javara.global.sse.service;

import com.team109.javara.global.sse.component.SseConnections;
import com.team109.javara.global.sse.controller.SseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

    private static final Long TIMEOUT = 300L * 60 * 1000;
    private final SseConnections sseConnections;

    //  Map으로 policeId별 sseemitter 설정
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long memberId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        this.emitters.put(memberId, emitter);
        sseConnections.add(memberId);

        // emitters 맵에서 제거
        emitter.onTimeout(() -> {
            log.info("SSE 타임아웃: member [{}]", memberId);
            this.emitters.remove(memberId);
            sseConnections.remove(memberId);
        });

        // 완료 emitters 맵에서 제거
        emitter.onCompletion(() -> {
            log.info("SSE 완료: member [{}]", memberId);
            this.emitters.remove(memberId);
            sseConnections.remove(memberId);
        });

        // 에러 emitters 맵에서 제거
        emitter.onError(e -> {
            log.error("SSE 에러: member [{}]", memberId, e);
            this.emitters.remove(memberId);
            sseConnections.remove(memberId);
        });


//        try {
//            emitter.send(SseEmitter.event()
//                    .id(memberId + "_" + System.currentTimeMillis()) // 이벤트 ID
//                    .name("connect") // 이벤트 이름 (클라이언트에서 구분용)
//                    .data("SSE connection established for police: " + memberId)
//                    .reconnectTime(10000)); // 재연결 시도 간격 (ms)
//        } catch (IOException e) {
//            log.error("Failed to send initial connect event for policeId: {}", memberId, e);
//            emitter.completeWithError(e); // 에러 발생 시 즉시 완료 처리
//        }

        return emitter;
    }


    public void sendEventToPolice(Long memberId, String eventName, Object data) {
        SseEmitter emitter = this.emitters.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(memberId + "_" + System.currentTimeMillis()) //emitter id 생성
                        .name(eventName)
                        .data(data)
                        .reconnectTime(10000));
                log.info("SSE 이벤트 [{}] 알림 보냄: member [{}]", eventName, memberId);
            } catch (IOException | IllegalStateException e) {
                log.error("SSE 알림 보내기 실패: member [{}]", memberId, e);
                this.emitters.remove(memberId);
            }
        } else {
            log.warn("SSE 연결된 경찰이 없습니다: member [{}]", memberId);
        }
    }

}