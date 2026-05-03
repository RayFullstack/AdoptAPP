package com.adoptapp.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 50)
    private String address;

    /*MEJORAR STATUS PARA QUE SE INICIALICE SOLO
    SEGUN CHATGPT :
     @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
    }
     */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}

