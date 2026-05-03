package com.adoptapp.petservice.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
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

    @Column(nullable = false, length = 50)
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
    private String health;

    @Column(nullable = false, length = 50)
    private String personality;

    /*MEJORAR STATUS PARA QUE SE INICIALICE SOLO
    SEGUN CHATGPT :
     @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = "AVAILABLE";
    }
     */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long fosterId;

}
