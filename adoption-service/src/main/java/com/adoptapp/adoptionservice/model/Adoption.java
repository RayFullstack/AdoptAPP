package com.adoptapp.adoptionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name= "adoptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Adoption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long petId;

    @Column(nullable = false, length = 50)
    private String petName; // Añadido para que el Service no de error

    @Column(nullable = false, length = 50)
    private String adopterName; // Añadido para que el Service no de error

    @Column(nullable = false, length = 50)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}