package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    Boolean existsByNameIgnoreCase(String name);

    List<Pet> findByNameIgnoreCase(String name);

    List<Pet> findAllByOrderByCreatedAtAsc();

    List<Pet> findByStatus(PetStatus status);
}
