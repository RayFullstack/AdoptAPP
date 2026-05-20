package com.adoptapp.petservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pet_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(length = 20)
    private String previousName;

    @Column(length = 20)
    private String newName;

    @Column(length = 20)
    private String previousStatus;

    @Column(length = 20)
    private String newStatus;

    @Column(name = "previous_foster_id")
    private Long previousFosterId;

    @Column(name = "new_foster_id")
    private Long newFosterId;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 255)
    private String comment;

    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }
}
