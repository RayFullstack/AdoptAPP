package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Pet> findByStatusIgnoreCase(String status);

    List<Pet> findAllByOrderByCreatedAtAsc();
}