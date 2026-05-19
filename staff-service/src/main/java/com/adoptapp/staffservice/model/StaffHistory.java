package com.adoptapp.staffservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "previous_position", length = 20)
    private String previousPosition;

    @Column(name = "new_position", length = 20)
    private String newPosition;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(name = "previous_phone", length = 20)
    private String previousPhone;

    @Column(name = "new_phone", length = 20)
    private String newPhone;

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
