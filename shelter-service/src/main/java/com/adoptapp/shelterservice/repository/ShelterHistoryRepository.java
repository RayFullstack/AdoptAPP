package com.adoptapp.shelterservice.repository;

import com.adoptapp.shelterservice.model.ShelterHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterHistoryRepository extends JpaRepository<ShelterHistory, Long> {

    List<ShelterHistory> findByShelterIdOrderByChangedAtDesc(Long shelterId);
}
