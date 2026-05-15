package com.adoptapp.petservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pet_health")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean vaccinated;

    private Boolean sterilized;

    private String diseases;

    @OneToOne(mappedBy = "health")
    private Pet pet;
}