package com.adoptapp.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(name = "previous_name", length = 50)
    private String previousName;

    @Column(name = "new_name", length = 50)
    private String newName;

    @Column(name = "previous_surname", length = 50)
    private String previousSurname;

    @Column(name = "new_surname", length = 50)
    private String newSurname;

    @Column(name = "previous_username", length = 50)
    private String previousUsername;

    @Column(name = "new_username", length = 50)
    private String newUsername;

    @Column(name = "previous_email", length = 150)
    private String previousEmail;

    @Column(name = "new_email", length = 150)
    private String newEmail;

    @Column(name = "previous_phone", length = 100)
    private String previousPhone;

    @Column(name = "new_phone", length = 100)
    private String newPhone;

    @Column(name = "previous_role", length = 20)
    private String previousRole;

    @Column(name = "new_role", length = 20)
    private String newRole;

    @Column(name = "previous_active")
    private Boolean previousActive;

    @Column(name = "new_active")
    private Boolean newActive;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 255)
    private String comment;
}

