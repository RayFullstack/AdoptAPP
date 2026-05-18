package com.adoptapp.adoptionservice.repository;

import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {

    List<Adoption> findByStatus(AdoptionStatus status);

    List<Adoption> findAllByOrderByCreatedAtAsc();



}
