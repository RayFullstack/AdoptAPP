package com.adoptapp.adoptionservice.repository;

import com.adoptapp.adoptionservice.model.AdoptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdoptionHistoryRepository extends JpaRepository<AdoptionHistory, Long> {

    List<AdoptionHistory> findByAdoptionIdOrderByCreatedAtDesc(Long adoptionId);
}
