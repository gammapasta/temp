package com.team109.javara.domain.member.dto;

import com.team109.javara.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MemberInfoResponseDto {

        private Long id;
        private String username;
        private String policeId; // Member 엔티티의 policeId와 매핑
        private String name;
        private String gender; // String으로 표시
        private String role; // Role Enum의 DisplayName 또는 Name 사용
        private String status;
        private BigDecimal penaltyPoints;
        private String createdAt;
        private String edgeDeviceId;

        // Member 엔티티로부터 DTO를 생성하는 정적 팩토리 메소드
        public static MemberInfoResponseDto fromEntity(Member member) {
            if (member == null) {
                return null;
            }
            return MemberInfoResponseDto.builder()
                    .id(member.getId())
                    .username(member.getUsername())
                    .name(member.getName())
                    .gender(member.getGender() != null ? member.getGender().getDisplayName() : null)
                    .role(member.getRole() != null ? member.getRole().getDisplayName() : null)
                    .policeId(member.getPoliceId()) // Member의 policeId 사용
                    .status(member.getMemberStatus() != null ? member.getMemberStatus().getDisplayName() : null)
                    .penaltyPoints(member.getPenaltyPoints())
                    .createdAt(member.getCreatedAt().toString())
                    .edgeDeviceId(member.getEdgeDeviceId())
                    .build();
        }
}
