package com.team109.javara.global;

import com.team109.javara.domain.edgeDevice.dto.VerificationResponseDto;
import com.team109.javara.domain.event.event.EdgeDeviceEvent;
import com.team109.javara.domain.image.dto.ImageResponse;
import com.team109.javara.domain.image.entity.Image;
import com.team109.javara.domain.image.service.ImageService;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.domain.task.dto.taskAndWantedDto;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.task.entity.enums.TaskStatus;
import com.team109.javara.domain.task.repository.TaskRepository;
import com.team109.javara.domain.tracking.service.TrackingDecisionService;
import com.team109.javara.domain.vehicle.component.WantedSet;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.domain.webSocket.service.WebSocketService;
import com.team109.javara.global.common.response.BaseResponse;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import com.team109.javara.global.sse.service.NotificationService;
import com.team109.javara.global.sse.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final NotificationService notificationService;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WantedSet wantedSet;

        @Autowired
        private DataSource dataSource;


    @Operation(summary = "chnage mem status")
    @PostMapping("/changemem")
    public void cm(){
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 컬럼 삭제
            stmt.execute("ALTER TABLE member DROP COLUMN member_status");

            // 다시 생성 (enum 값은 프로젝트 정의와 맞게 수정)
            stmt.execute("ALTER TABLE member ADD COLUMN member_status ENUM('ACTIVE','INACTIVE','NOT_AVAILABLE','TRACKING','USER') DEFAULT 'INACTIVE' ");

            log.info( "Enum column fixed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            log.info("Error fixing enum: " + e.getMessage());
        }

    }

    @Operation(summary = "[테스트 전용] 수배차량 검증, deviceId -> 일반시민꺼")
    @PostMapping(value = "/verification")
    public BaseResponse<VerificationResponseDto> verifyTest(@RequestParam(value = "deviceId", required = true) String deviceId,
                                                        @RequestParam(value = "wantedVehicleNumber", required = true) String wantedVehicleNumber
    ){
        Member member = memberRepository.findByEdgeDeviceId(deviceId).orElseThrow(()-> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        WantedVehicle wantedVehicle = wantedVehicleRepository.findByWantedVehicleNumber(wantedVehicleNumber).orElseThrow(()-> new GlobalException(ErrorCode.VEHICLE_NOT_FOUND));

        if(wantedVehicle.getWantedVehicleStatus() != WantedVehicleStatus.WANTED){
            return BaseResponse.fail("수배중이 아닙니다", HttpStatus.CONFLICT);
        }


        VerificationResponseDto responseDto = new VerificationResponseDto();
        responseDto.setWantedVehicleNumber(wantedVehicle.getWantedVehicleNumber());
        responseDto.setWantedVehicleStatus(wantedVehicle.getWantedVehicleStatus());

        if(member.getRole()==Role.USER){
            wantedSet.add(wantedVehicleNumber); // 서버 주도 임무 부여를 위해 수배차량 넣기
            log.info("임시 wantedset 넣기 성공, {}", wantedSet.size());
        }

        return BaseResponse.success("수배차량입니다", responseDto);
    }

    @Operation(summary = "특정 패널티 내리기")
    @PostMapping(value = "/reducePenalty")
    public void reducePenalty(@RequestParam Long memberId){
            Member member = memberRepository.findById(memberId).orElseThrow(()-> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
            if (member != null) {
                log.info("특정 패널티 내리기");
                member.reducePenalty(member);
                memberRepository.save(member);
            }
            log.info("맴버 패널티 {}",member.getPenaltyPoints());



    }
    @Operation(summary = "sse로 메시지 보내기 테스트 컨트롤러")
    @PostMapping(value = "/sse/send")
    public void sendSse(@RequestParam Long memberId,
                                              @RequestParam String wantedVehicleNumber,
                                              @RequestParam Long taskId){

        notificationService.notifyForTaskDecision(memberId, wantedVehicleNumber ,taskId);


    }
    @Transactional
    @Operation(summary = "특정 task assigned 로 바꾸기")
    @PostMapping(value = "/task/assigned")
    public void changeTask(@RequestParam Long memberId,
            @RequestParam Long wantedVehicleId,
                        @RequestParam Long taskId){
            Task task = taskRepository.findById(taskId).orElseThrow();
            task.setTaskStatus(TaskStatus.ASSIGNED);
            WantedVehicle wantedVehicle = wantedVehicleRepository.findById(wantedVehicleId).orElseThrow();
            wantedVehicle.setWantedVehicleStatus(WantedVehicleStatus.TRACKING);
            taskRepository.save(task);
            wantedVehicleRepository.save(wantedVehicle);

        notificationService.notifyForTaskDecision(memberId, wantedVehicle.getWantedVehicleNumber(), task.getTaskId());
        log.info("Task [{}] 상태 [ASSIGNED], 경찰한테 알림 보내기 성공 member [{}]", task.getTaskId(), memberId);

    }
    @Operation(summary = "특정 task assigned 로 바꾸기")
    @PostMapping(value = "/task/changemember")
    public void changeTaskMember(@RequestParam Long memberId,
                           @RequestParam Long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setAssignedMember(memberRepository.findById(memberId).orElseThrow());
        taskRepository.save(task);

        log.info("Task 변경 성공 member [{}]", memberId);

    }


    @Operation(summary = "이건 작동 안함 이거 사용")
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


    @Operation(summary = "sse 테스트할때 사용할 task와 수배차량 가져오기")
    @GetMapping("taskAndWanted")
    public BaseResponse<List<taskAndWantedDto>> getTaskAndWanted() {
        List<Task> tasks = taskRepository.findAll();
        List<taskAndWantedDto> dto = tasks.stream().map(taskAndWantedDto::from).toList();
        return BaseResponse.success("task반환",dto);
    }
}

