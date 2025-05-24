package com.team109.javara.domain.event.event;

public class TaskEvent {
    private final Long taskId;
    private final Long wantedVehicleId;

    public TaskEvent(Long taskId, Long wantedVehicleId) {
        this.taskId = taskId;
        this.wantedVehicleId = wantedVehicleId;
    }

    public Long getTaskId() {
        return taskId;
    }
    public Long getWantedVehicleId() {
        return wantedVehicleId;
    }
}
