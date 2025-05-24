package com.team109.javara.domain.webSocket.service;

import com.team109.javara.domain.location.entity.PoliceLocation;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.location.repository.PoliceLocationRepository;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.location.repository.WantedVehicleLocationRepository;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.domain.webSocket.dto.DeviceLocationDto;
import com.team109.javara.domain.webSocket.dto.SessionCommand;
import com.team109.javara.domain.webSocket.registry.SessionRegistry;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

//메시지 처리 및 /sub/{deviceId} 푸시
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {
    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WantedVehicleRepository wantedVehicleRepository;

    @Autowired
    private WantedVehicleLocationRepository wantedVehicleLocationRepository;

    @Autowired
    private PoliceLocationRepository policeLocationRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final CommandService commandService;


    //디바이스 최초 연결 시 초기 상태 알림
    public void initializeMemberStatus(String deviceId) {
        log.info("[Websocket] [init] 엣지디바이스: [{}]", deviceId);
        messagingTemplate.convertAndSend("/topic/" + deviceId,
                Map.of("memberStatus", "INITIALIZED"));
    }

    // 위치 처리 후 응답
    public void processLocation(String deviceId, DeviceLocationDto deviceLocationDto) {
        SessionCommand stopCommand = new SessionCommand();
        try {
            if (deviceLocationDto.getLatitude() == null || deviceLocationDto.getLongitude() == null) {
                log.warn("[Websocket] 위치 정보가 없습니다: 엣지디바이스 [{}]", deviceId);
                return;
            }
            Optional<Member> memberOptional = memberRepository.findByEdgeDeviceId(deviceId);
            if (memberOptional.isEmpty()) {
                log.warn("[Websocket] 엣지디바이스 [{}]가 연결했지만, 맴버를 찾을 수 없습니다.", deviceId);
                stopCommand.setCommand("stop");
                commandService.sendCommandToDevice(deviceId, stopCommand);
                return;
            }

            Member member = memberOptional.get();
            Long memberId = member.getId();
            Role memberRole = member.getRole();
            MemberStatus memberStatus = member.getMemberStatus();

            BigDecimal latitude = deviceLocationDto.getLatitude();
            BigDecimal longitude = deviceLocationDto.getLongitude();
            Long wantedVehicleId = deviceLocationDto.getWantedVehicleId();
            log.info("[Websocket] 위치 저장 시작: member [{}], role [{}], status [{}], lat [{}], lon [{}], wantedVehicleId [{}], deviceId [{}]", memberId, memberRole, memberStatus, latitude, longitude, wantedVehicleId, deviceId);

            // 3. 데이터 저장 분기
            if (memberRole == Role.USER && wantedVehicleId != null) {
                log.info("[Websocket] Role [USER] 수배차량 [{}]. WantedVehicleLocation에 저장",wantedVehicleId);
                saveWantedVehicleLocation(latitude, longitude, member.getReporterName(), wantedVehicleId, memberId, deviceId);
            } else if (memberRole == Role.POLICE) {
                if ((memberStatus == MemberStatus.TRACKING || memberStatus == MemberStatus.NOT_AVAILABLE) && wantedVehicleId != null) {
                    log.info("[Websocket] Role [POLICE] Status [TRACKING,NOT_AVAILABLE] 수배차량 [{}]. WantedVehicleLocation에 저장",wantedVehicleId);
                    saveWantedVehicleLocation(latitude, longitude, member.getReporterName(), wantedVehicleId, memberId, deviceId);
                } else if (memberStatus == MemberStatus.ACTIVE) {
                    log.info("[Websocket] Role [POLICE] Status [ACTIVE]. PoliceLocation에 저장");
                    saveMemberLocation(member, latitude, longitude, memberId, deviceId);
                } else {
                    stopCommand.setCommand("stop");
                    log.warn("[Websocket] 경찰 위치를 받았지만 처리가 불가능 합니다. Role: [{}], Status: [{}], WantedVehicleId: [{}], Device: [{}]",
                            memberRole, memberStatus, wantedVehicleId, deviceId);
                }
            } else {
                stopCommand.setCommand("stop");
                log.warn("[Websocket] 위치를 admin으로 부터 받았습니다. 혹은 wantedVehicleId이 없습니다. Role: {}, Device: {}", memberRole, deviceId);
            }
        }

        catch (GlobalException e) {
        log.error("[Websocket] 에러 deviceId [{}] [{}]", deviceId, e.getMessage());
        }
        catch (Exception e) {
        log.error("[Websocket] 웹소켓으로 부터 메시지를 받았지만 에러가 발생했습니다 deviceId [{}],  error {}", deviceId, e.getMessage(), e);
    }

    }

    private void saveWantedVehicleLocation(BigDecimal latitude, BigDecimal longitude, String reporterName, Long wantedVehicleId, Long memberId, String deviceId) {
        WantedVehicle wantedVehicle = wantedVehicleRepository.findById(wantedVehicleId)
                .orElseThrow(() -> {
                    log.error("[Websocket] 수배차량을 찾지 못했습니다 위치 저장이 불가능 합니다. wantedVehicleId [{}], member [{}], deviceId [{}]", wantedVehicleId, memberId, deviceId);
                    SessionCommand stopCommand = new SessionCommand();
                    stopCommand.setCommand("stop");
                    commandService.sendCommandToDevice(deviceId, stopCommand);
                    return new GlobalException(ErrorCode.WANTED_VEHICLE_NOT_FOUND, "수배 차량 정보를 찾을 수 없습니다. ID: " + wantedVehicleId);
                });

        WantedVehicleLocation wantedVehicleLocation = new WantedVehicleLocation();
        wantedVehicleLocation.setLatitude(latitude);
        wantedVehicleLocation.setLongitude(longitude);
        wantedVehicleLocation.setReporterName(reporterName);
        wantedVehicleLocation.setSightedAt(LocalDateTime.now());
        wantedVehicleLocation.setWantedVehicle(wantedVehicle);

        wantedVehicleLocationRepository.save(wantedVehicleLocation);
        log.info("[Websocket] 수배차량 위치 저장 성공! 수배차량: [{}] member: [{}] 엣지디바이스: [{}]", wantedVehicleId, memberId, deviceId);
    }

    private void saveMemberLocation(Member member, BigDecimal latitude, BigDecimal longitude, Long memberId, String deviceId) {
        PoliceLocation newLocation = new PoliceLocation();
        newLocation.setMember(member);
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);

        policeLocationRepository.save(newLocation);
        log.info("[Websocket] 경찰 위치 저장 성공: member: [{}] 엣지디바이스:[{}]", memberId, deviceId);
    }

}
