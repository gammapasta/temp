package com.team109.javara.domain.webSocket.eventListener;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.webSocket.dto.SessionCommand; // SessionCommand 임포트 추가
import com.team109.javara.domain.webSocket.registry.SessionRegistry;
import com.team109.javara.domain.webSocket.service.WebSocketService;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired; // RequiredArgsConstructor 사용 시 불필요
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor // final 필드를 위한 생성자 자동 생성
@Slf4j
public class WebSocketEventListener {

    // final 로 선언하여 생성자 주입 사용 (권장)
    private final SessionRegistry sessionRegistry;
    private final WebSocketService WebSocketService;

    // 구독 목적지 경로 상수화 (선택 사항이지만 권장)
    private static final String COMMAND_SUBSCRIBE_DESTINATION = "/user/topic/device/command";
    private final MemberRepository memberRepository;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String deviceId = accessor.getFirstNativeHeader("device-id");

        if (deviceId != null && sessionId != null && !deviceId.isEmpty()) { // sessionId null 체크 추가
            sessionRegistry.add(sessionId, deviceId);
            // 로그 메시지 명확화
            log.info("엣지디바이스 연결: [{}] (세션: [{}])", deviceId, sessionId);


        } else {
            // 로그 메시지 명확화
            log.warn("엣지디바이스 id or 세션 id 못찾았습니다. Headers: {}. SessionId: {}", accessor.toNativeHeaderMap(), sessionId);
        }
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userDestination = accessor.getFirstNativeHeader("destination"); // 클라이언트가 보낸 원본 destination
        String subscriptionId = accessor.getSubscriptionId();

        log.debug("세션 [{}] [SUBSCRIBE] 메시지 보냈습니다. 도착지: [{}] 구독id: [{}]", sessionId, userDestination, subscriptionId);

        if (userDestination != null && userDestination.equals(COMMAND_SUBSCRIBE_DESTINATION)) {
            if (sessionId != null) {
                String deviceId = sessionRegistry.getDeviceId(sessionId);
                if (deviceId != null) {
                    log.info("엣지디바이스 [{}] (세션: [{}]) 명령 topic에 구독했습니다 [{}]", deviceId, sessionId, userDestination);
    
                    Member member = memberRepository.findByEdgeDeviceId(deviceId)
                            .orElseThrow(() -> new GlobalException(ErrorCode.DEVICE_NOT_FOUND));
                    // 처음 접속시 시민일경우
                    SessionCommand startCommand = new SessionCommand();
                    if(member.getRole() == Role.USER){
                        startCommand.setCommand("user");
                        log.info("일반 시민 접속");
                    }else {
                        startCommand.setCommand("start");
                        log.info("경찰 접속");
                    }
                } else {
                    log.warn("세션[{}]에서 엣지디바이스id 찾기 실패. 목적지: [{}]", sessionId, userDestination);
                }
            } else {
                log.warn("목적지 [{}] 에서 세션이 null 입니다", userDestination);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            String deviceId = sessionRegistry.getDeviceId(sessionId);
            sessionRegistry.removeBySessionId(sessionId);
            if (deviceId != null) {
                log.info("엣지디바이스: [{}] [연결종료] (세션: {})", deviceId, sessionId);
            } else {
                log.info("세션 연결 종료. 세션 [{}]", sessionId);
            }
        } else {
            log.warn("세션이 null 입니다");
        }
    }

}