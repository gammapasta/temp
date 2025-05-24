package com.team109.javara.domain.task.dto;

import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.task.entity.enums.TaskStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class taskAndWantedDto{
    private Long taskId;
    private Long wantedVehicleId;
    private Long assignedMember;
    private TaskStatus taskStatus;
    public taskAndWantedDto(Long taskId, Long wantedVehicleId, Long assignedMember, TaskStatus taskStatus) {
        this.taskId = taskId;
        this.wantedVehicleId = wantedVehicleId;
        this.assignedMember = assignedMember;
        this.taskStatus = taskStatus;
    }


    public static taskAndWantedDto from(Task task) {
        return new taskAndWantedDto(
                task.getTaskId(),
                task.getWantedVehicle() != null ? task.getWantedVehicle().getWantedVehicleId() : null,
                task.getAssignedMember() != null ? task.getAssignedMember().getId() : null,
                task.getTaskStatus()
        );
    }
}