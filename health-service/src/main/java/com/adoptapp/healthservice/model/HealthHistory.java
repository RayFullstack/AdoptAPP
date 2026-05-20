package com.adoptapp.healthservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_id", nullable = false)
    private Health health;

    @Column(name = "previous_sterilization_status", length = 150)
    private String previousSterilizationStatus;

    @Column(name = "new_sterilization_status", length = 150)
    private String newSterilizationStatus;

    @Column(name = "previous_vaccination_status", length = 150)
    private String previousVaccinationStatus;

    @Column(name = "new_vaccination_status", length = 150)
    private String newVaccinationStatus;

    @Column(name = "previous_disease", length = 150)
    private String previousDisease;

    @Column(name = "new_disease", length = 150)
    private String newDisease;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 255)
    private String comment;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }
}


