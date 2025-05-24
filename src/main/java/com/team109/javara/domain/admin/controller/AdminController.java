package com.team109.javara.domain.admin.controller;

import com.team109.javara.domain.admin.dto.SessionConnectionsResponseDto;
import com.team109.javara.domain.webSocket.component.SessionConnections;
import com.team109.javara.global.common.response.BaseResponse;
import com.team109.javara.global.sse.component.SseConnections;
import com.team109.javara.domain.admin.dto.SseConnectionsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final SseConnections sseConnections;
    private final SessionConnections sessionConnections;

    @Operation(summary = "어드민 sse 커넥션 수 보기")
    @GetMapping(value = "/sse-connections")
    public BaseResponse<SseConnectionsResponseDto> getSseConnections() {
        SseConnectionsResponseDto sseConnectionsResponseDto = new SseConnectionsResponseDto();
        sseConnectionsResponseDto.setSseConnectionsCount(sseConnections.size());
        return BaseResponse.success("sse에 접속한 엣지 디바이스 수 ", sseConnectionsResponseDto);
    }

    @Operation(summary = "어드민 웹소켓 커넥션 수 보기")
    @GetMapping(value = "websocket-connections")
    public BaseResponse<SessionConnectionsResponseDto> getWebSocketConnections() {
        SessionConnectionsResponseDto sessionConnectionsResponseDto = new SessionConnectionsResponseDto();
        sessionConnectionsResponseDto.setSessionConnectionsCount(sessionConnections.size());
        return BaseResponse.success("웹소켓 접속한 엣지 디바이스 수 ", sessionConnectionsResponseDto);
    }
}
