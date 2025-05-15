package com.team109.javara.domain.location.entity;

import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wanted_vehicles_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WantedVehicleLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId; // Added separate PK

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "reporter_name", nullable = false, length = 100)
    private String reporterName;

    @Column(name = "sighted_at", nullable = false)
    private LocalDateTime sightedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wanted_vehicle_id", nullable = false)
    private WantedVehicle wantedVehicle;
}
