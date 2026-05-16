package com.adoptapp.adoptionservice.repository;

import com.adoptapp.adoptionservice.model.Adoption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {

    List<Adoption> findByStatusIgnoreCase(String status);

    List<Adoption> findAllByOrderByCreatedAtAsc();
}
