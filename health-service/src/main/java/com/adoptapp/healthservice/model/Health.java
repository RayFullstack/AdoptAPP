package com.adoptapp.healthservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "health")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Health {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long petId;

    @OneToMany(mappedBy = "health", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<HealthHistory> history = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SterilizationStatus sterilizationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VaccinationStatus vaccinationStatus;

    @Column(length = 500, nullable = false)
    private String diseases;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthStatus status = HealthStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (diseases == null) {
            diseases = "Ninguna";
        }
        if (status == null) {
            status = HealthStatus.ACTIVE;
        }
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
            updatedAt = LocalDateTime.now();
    }
}
