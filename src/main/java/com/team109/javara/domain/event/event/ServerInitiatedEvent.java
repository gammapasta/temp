package com.team109.javara.domain.event.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerInitiatedEvent {
    private final Long memberId;
    private final String wantedVehicleNumber;
}
