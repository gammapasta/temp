package com.team109.javara.domain.vehicle.entity;

import com.team109.javara.domain.image.entity.Image;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wanted_vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WantedVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wanted_vehicle_id")
    private Long wantedVehicleId;

    @Column(name = "wanted_vehicle_number", nullable = false, unique = true, length = 30)
    private String wantedVehicleNumber;

    @Column(name = "case_number", nullable = false, unique = true, length = 50)
    private String caseNumber;

    @Column(name = "crime_type", nullable = false, length = 100)
    private String crimeType;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "wanted_vehicle_status", nullable = false, length = 20)
    private WantedVehicleStatus wantedVehicleStatus;

    @Lob
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relationships ---


    @OneToMany(mappedBy = "wantedVehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WantedVehicleLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "wantedVehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "wantedVehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();



    public void addLocation(WantedVehicleLocation location) {
        locations.add(location);
        location.setWantedVehicle(this);
    }
    public void removeLocation(WantedVehicleLocation location) {
        locations.remove(location);
        location.setWantedVehicle(null);
    }
    public void addTask(Task task) {
        tasks.add(task);
        task.setWantedVehicle(this);
    }
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setWantedVehicle(null);
    }

    // 헬퍼 메서드
    public void addImage(Image image) {
        images.add(image);
        image.setWantedVehicle(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setWantedVehicle(null);
    }
}