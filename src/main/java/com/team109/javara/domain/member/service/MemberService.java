package com.team109.javara.domain.member.service;

import com.team109.javara.domain.member.dto.MemberPenaltyPointsDto;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.webSocket.dto.SessionCommand;
import com.team109.javara.domain.webSocket.service.CommandService;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CommandService commandService;

    //맴버 검증
    public Member getCurrentMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new GlobalException(ErrorCode.AUTHENTICATION_FAILED);
        }
        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        return member;
    }

    //맴버가 경찰인지 검증
    public Member getCurrentMemberPolice(UserDetails userDetails) {
        if (userDetails == null) {
            throw new GlobalException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() != Role.POLICE) {
            throw new GlobalException(ErrorCode.AUTHORIZATION_FAILED);
        }

        return member;
    }

    public void verifyCurrentMember(UserDetails userDetails) {
        if (userDetails == null) {
            throw new GlobalException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }


    @Transactional
    public void updateMemberStatus(Member member , MemberStatus newStatus) {
        String deviceId;
        Long memberId = member.getId();
        Role role = member.getRole();
        MemberStatus oldStatus = member.getMemberStatus(); //로그 저장용

        try{
            deviceId = member.getEdgeDeviceId();
        } catch (NullPointerException e) {
            throw new GlobalException(ErrorCode.DEVICE_NOT_FOUND, "디바이스id 못찾음: " + memberId);
        }

        if (member.getMemberStatus() == newStatus) {
            log.info("Member [{}] 현재 상태와 같음 newStatus [{}].", memberId, newStatus);
            throw new GlobalException(ErrorCode.DUPLICATE_STATUS);
        }

        SessionCommand command = new SessionCommand();

        if(role == Role.POLICE){
            log.info("경찰임");
            switch (newStatus) {
                case ACTIVE:
                    command.setCommand("active");
                    break;
                case TRACKING:
                case NOT_AVAILABLE:
                    command.setCommand("wanted");
                    break;
                case INACTIVE:
                    command.setCommand("stop");
                    break;
                default:
                    throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }else if(role == Role.USER){
            log.info("일반인임");
            if (newStatus == MemberStatus.USER) {
                command.setCommand("user");
            }else if (newStatus == MemberStatus.INACTIVE) {
                command.setCommand("stop");
            }else {
                throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        member.setMemberStatus(newStatus);
        memberRepository.save(member);

        log.info("Update member [{}] status old [{}] -> new [{}].", memberId, oldStatus, newStatus);
        commandService.sendCommandToDevice(deviceId, command);
    }

    //TODO: 엣지디바이스 중복 로직
    @Transactional
    public void updateEdgeDeviceId(Member member, String newEdgeDeviceId) {
        Long memberId = member.getId();
        Role role = member.getRole();
        String oldDeviceId = member.getEdgeDeviceId();
        MemberStatus currentStatus = member.getMemberStatus();
        SessionCommand newCommand = new SessionCommand();

        // 1. 상태 변경이 없으면 종료
        if (member.getEdgeDeviceId() == newEdgeDeviceId) {
            log.info("Member의 [{}] edgeDeviceId 같음 [{}].", memberId, newEdgeDeviceId);
            throw new GlobalException(ErrorCode.DUPLICATE_EDGE_DEVICE_ID);
        }
        // 2. 중복 엣지디바이스 제거
        if(memberRepository.findByEdgeDeviceId(newEdgeDeviceId).isPresent()){
            log.info("Member의 [{}] edgeDeviceId [{} 누군가 사용중임.", memberId, newEdgeDeviceId);
            throw new GlobalException(ErrorCode.DUPLICATE_EDGE_DEVICE_ID, "누군사 사용중인 엣지디바이스 아이디");
        }
        
        // 3. 기존 디바이스에 stop 명령 전송
        if (oldDeviceId != null) {
            SessionCommand stopCommand = new SessionCommand();
            stopCommand.setCommand("stop");
            commandService.sendCommandToDevice(oldDeviceId, stopCommand);
            log.info("Sent [stop] command -> old device {}", oldDeviceId);
        }

        // 3. 새 디바이스에 현재 상태에 따른 명령 전송
        if (role == Role.POLICE) {
            switch (currentStatus) {
                case ACTIVE:
                    newCommand.setCommand("active");
                    break;
                case TRACKING:
                case NOT_AVAILABLE:
                    newCommand.setCommand("wanted");
                    break;
                default:
                    newCommand.setCommand("stop");
                    break;
            }
        } else if (role == Role.USER) {
            if (currentStatus == MemberStatus.USER) {
                newCommand.setCommand("user");
            } else {
                newCommand.setCommand("stop");
            }
        }

        // 4. DB 상태 업데이트
        member.setEdgeDeviceId(newEdgeDeviceId);
        memberRepository.save(member);
        log.info("Updated member [{}] EdgeDeviceId[{}].", memberId, newEdgeDeviceId);

        //5. 새로운 엣지 디바이스한테 메시지 모냄
        commandService.sendCommandToDevice(newEdgeDeviceId, newCommand);
        log.info("Sent [{}] command -> new device [{}] status [{}]", newCommand, newEdgeDeviceId, currentStatus);
    }


    public MemberPenaltyPointsDto getPenaltyPoints(Member member) {

        return MemberPenaltyPointsDto.fromEntity(member);
    }





}