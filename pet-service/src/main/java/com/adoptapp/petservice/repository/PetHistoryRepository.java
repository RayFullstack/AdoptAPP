package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.PetHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetHistoryRepository extends JpaRepository<PetHistory, Long> {
    List<PetHistory> findByPetIdOrderByChangedAtDesc(Long petId);
}
