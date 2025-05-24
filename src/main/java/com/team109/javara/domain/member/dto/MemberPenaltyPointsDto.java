package com.team109.javara.domain.member.dto;


import com.team109.javara.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Builder
public class MemberPenaltyPointsDto {
    private Long memberId;
    private Double penaltyPoints;

    public static MemberPenaltyPointsDto fromEntity(Member member) {
        return MemberPenaltyPointsDto.builder()
                .memberId(member.getId())
                .penaltyPoints(member.getPenaltyPoints())
                .build();
    }
}
