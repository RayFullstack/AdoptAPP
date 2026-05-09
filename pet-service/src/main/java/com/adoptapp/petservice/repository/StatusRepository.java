package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusRepository extends JpaRepository<PetStatus, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<PetStatus> findByNameIgnoreCase(String name);

}