package com.team109.javara.global;

import com.team109.javara.domain.image.dto.ImageResponse;
import com.team109.javara.domain.image.entity.Image;
import com.team109.javara.domain.image.service.ImageService;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.domain.webSocket.service.WebSocketService;
import com.team109.javara.global.common.response.BaseResponse;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import com.team109.javara.global.sse.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class test {
    private final MemberRepository memberRepository;
    private final WebSocketService webSocketService;
    private final SseEmitterService sseEmitterService;
    private final WantedVehicleRepository wantedVehicleRepository;
    private final ImageService imageService;

    @Operation(summary = "이미지 업로드 테스트")
    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "memberId", required = false) Long memberId, // memberId는 선택적일 수 있음
            @RequestParam("wantedVehicleId") Long wantedVehicleId) { // wantedVehicleId는 필수라고 가정

        try {
            // memberId나 wantedVehicleId를 사용하여 실제 Member, WantedVehicle 엔티티를 조회
            // 실제로는 ID만 넘기는 것보다, 인증된 사용자 정보를 활용하거나 다른 DTO로 받을 수 있음
            Member member = null;
            if (memberId != null) {
                member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
            }

            WantedVehicle wantedVehicle = wantedVehicleRepository.findById(wantedVehicleId)
                    .orElseThrow(() -> new RuntimeException("WantedVehicle not found with id: " + wantedVehicleId));

            Image savedImage = imageService.storeImage(file, member, wantedVehicle);

            ImageResponse response = new ImageResponse(
                    savedImage.getImageId(),
                    savedImage.getImageUrl() // 프론트에서 사용할 URL
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // 간단한 예외 처리, 실제로는 더 상세한 예외 처리 필요
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageResponse(null, null));
        }
    }

    @Operation(summary = "테스트용 이거 사용")
    @GetMapping(value = "/sse/connect/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connectTest(@PathVariable Long id) {

        try {
            SseEmitter emitter = sseEmitterService.createEmitter(id);
            log.info("SSE emitter created successfully for policeId: {}", id);
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("Error creating SSE emitter for policeId: {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }


    @Operation(summary = "유저 아이디 기반으로 어드민 만들기", description = "어드민 만들기")
    @GetMapping(value = "{username}")
    public BaseResponse makeAdmin(@PathVariable String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(()-> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
                member.setAdmin(Role.ADMIN);
                memberRepository.save(member);
        return BaseResponse.success("유저를 어드민으로 만들기 성공");
    }



    @GetMapping("/test/get")
    public BaseResponse<MemberStatus> getTest() {
        Member member = memberRepository.findById(1L).orElseThrow();

        return BaseResponse.success("상태 변경 성공", member.getMemberStatus());
    }



}
