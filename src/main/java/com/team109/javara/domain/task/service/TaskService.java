package com.team109.javara.domain.task.service;

import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.task.entity.enums.TaskStatus;
import com.team109.javara.domain.task.repository.TaskRepository;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    @Transactional
    public void createTask(WantedVehicle wantedVehicle) {
        Task task = new Task();
        task.setWantedVehicle(wantedVehicle);
        task.setTaskStatus(TaskStatus.ACTIVE);
        taskRepository.save(task);

        log.info("수배차량[{}]: 새로운 임무 [{}] 생성 완료", wantedVehicle.getWantedVehicleNumber(), task.getTaskId());
    }

    @Transactional
    public void taskCompleted(Task task){

        task.setTaskStatus(TaskStatus.COMPLETED);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        log.info("수배차량[{}]: 임무 [{}] 완료", task.getWantedVehicle().getWantedVehicleNumber(), task.getTaskId());
    }
}
