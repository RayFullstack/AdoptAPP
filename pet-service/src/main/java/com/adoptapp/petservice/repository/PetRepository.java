package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Pet> findByNameIgnoreCase(String name);

    List<Pet> findAllByOrderByCreatedAtAsc();

    List<Pet> findByStatus_NameIgnoreCase(String statusFilter);
}
