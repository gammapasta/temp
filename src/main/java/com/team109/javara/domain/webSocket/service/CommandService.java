package com.team109.javara.domain.webSocket.service;

import com.team109.javara.domain.webSocket.dto.SessionCommand;
import com.team109.javara.domain.webSocket.registry.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;


    public void sendCommandToDevice(String deviceId, SessionCommand command) {
        String sessionId = sessionRegistry.getSessionId(deviceId);
        if (sessionId != null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/device/command", command, createHeaders(sessionId)); //전송이 안되던 이유가 header가 없어서 참고: puzzle-making.tistory.com/291
            log.info("[Websocket] 명령 [{}]을 엣지디바이스: [{}]로 보냈습니다! (세션: {})", command, deviceId, sessionId);

        } else {
            log.warn("[Websocket] 엣지디바이스 [{}] 세션을 못 찾았습니다", deviceId);
        }
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();

    }
}
