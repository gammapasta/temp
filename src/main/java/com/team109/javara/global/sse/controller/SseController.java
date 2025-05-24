package com.team109.javara.global.sse.controller;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.global.sse.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final MemberService memberService;


    @Operation(summary = "sse 연결")
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.getCurrentMember(userDetails);
        Long memberId = member.getId();
        try {
            SseEmitter emitter = sseEmitterService.createEmitter(memberId);
            log.info("SSE 연결 성공 member [{}]", memberId);
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("SSE 연결 실패 member {}", memberId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
