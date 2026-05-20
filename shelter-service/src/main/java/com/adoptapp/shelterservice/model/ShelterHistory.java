package com.adoptapp.shelterservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shelter_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShelterHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "previous_name", length = 100)
    private String previousName;

    @Column(name = "new_name", length = 100)
    private String newName;

    @Column(name = "previous_email", length = 100)
    private String previousEmail;

    @Column(name = "new_email", length = 100)
    private String newEmail;

    @Column(name = "previous_phone", length = 20)
    private String previousPhone;

    @Column(name = "new_phone", length = 20)
    private String newPhone;

    @Column(name = "previous_description", columnDefinition = "TEXT")
    private String previousDescription;

    @Column(name = "new_description", columnDefinition = "TEXT")
    private String newDescription;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(name = "previous_active")
    private Boolean previousActive;

    @Column(name = "new_active")
    private Boolean newActive;

    @Column(length = 255)
    private String comment;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }
}
