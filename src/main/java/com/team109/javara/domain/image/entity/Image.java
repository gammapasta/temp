package com.team109.javara.domain.image.entity;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
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
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Lob // 긴 문자열 저장
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @CreationTimestamp // Automatically set on creation
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wanted_vehicle_id")
    private WantedVehicle wantedVehicle;


}
