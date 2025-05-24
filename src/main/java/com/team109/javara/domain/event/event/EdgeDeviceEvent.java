package com.team109.javara.domain.event.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EdgeDeviceEvent {
    private final String edgeDeviceId;
    private final String wantedVehicleNumber;
}
