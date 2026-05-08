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
    Long id;

    @Column(nullable = false, length = 50)
    Long userId;

    @Column(nullable = false, length = 50)
    Long petId;

    @Column(nullable = false, length = 50)
    String status; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at")
    LocalDateTime createdAt;



}
