package com.adoptapp.petservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name= "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 50)
    private String species;

    @Column(nullable = false, length = 50)
    private String race;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 50)
    private String size;

    @Column(nullable = false, length = 50)
    private String personality;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long fosterId;

    @Column(name = "shelter_id")
    private Long shelterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "health_id")
    private PetHealth health;

}
