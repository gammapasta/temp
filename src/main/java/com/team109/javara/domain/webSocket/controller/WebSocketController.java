package com.team109.javara.domain.webSocket.controller;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.webSocket.dto.DeviceLocationDto;
import com.team109.javara.domain.webSocket.dto.SessionCommand;
import com.team109.javara.domain.webSocket.registry.SessionRegistry;
import com.team109.javara.domain.webSocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Optional;

//클라이언트의 /pub 메시지 핸들링, /app
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService WebSocketService;

    @Autowired
    private SessionRegistry sessionRegistry;
    @Autowired
    private MemberRepository memberRepository;


    /** 2) 위치 데이터 수신 */
    @MessageMapping("/location")
    public void receiveLocation(@Header("simpSessionId") String sessionId,
                         @Payload DeviceLocationDto dto) {
        String deviceId = sessionRegistry.getDeviceId(sessionId);
        log.info("location: sessionId={}, deviceId={}, data={}", sessionId, deviceId, dto);
        WebSocketService.processLocation(deviceId, dto);
    }

    // 클라이언트가 /app/device/command 구독하면 이 메소드가 호출
    @SubscribeMapping("/device/command")
    public SessionCommand handleDeviceCommandSubscription(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String deviceId = sessionRegistry.getDeviceId(sessionId);

        if (deviceId != null) {
            //보내라고 명령 줌
            SessionCommand command = new SessionCommand();

            Optional<Member> memberOptional = memberRepository.findByEdgeDeviceId(deviceId);
            if (memberOptional.isEmpty()) {
                log.warn("엣지디바이스: [{}] connected (WebSocket), 맴버 못찾았습니다", deviceId);
                command.setCommand("stop");
                return command;
            }
            MemberStatus currentStatus = memberOptional.get().getMemberStatus();

            switch (currentStatus) {
                case ACTIVE:
                    command.setCommand("active");
                    break;
                case TRACKING:
                case NOT_AVAILABLE:
                    command.setCommand("wanted");
                    break;
                case USER:
                    command.setCommand("user");
                    break;
                default: // INACTIVE or 일반 사용자면 일단 멈춤
                    command.setCommand("stop");
                    break;
            }

            return command;
        } else {
            log.warn("session [{}] 해당하는 deviceId 못찾았습니다. @SubscribeMapping에서 /topic/device/command 경로로 command 못보냈어요.", sessionId);
            return null;
        }
    }


}
