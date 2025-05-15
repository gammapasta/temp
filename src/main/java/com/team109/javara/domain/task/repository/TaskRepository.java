package com.team109.javara.domain.task.repository;

import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository  extends JpaRepository<Task, Long> {
    Optional<Task> findByWantedVehicle(WantedVehicle wantedVehicle);
    Optional<Task> findByWantedVehicle_WantedVehicleNumber(String id);
}
