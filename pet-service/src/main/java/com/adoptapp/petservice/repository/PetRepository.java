package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Pet> findByNameIgnoreCase(String name);

    List<Pet> findAllByOrderByCreatedAtAsc();

    List<Pet> findByStatusIgnoreCase(PetStatus status);
}
