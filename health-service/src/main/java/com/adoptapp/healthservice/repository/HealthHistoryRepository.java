package com.adoptapp.healthservice.repository;

import com.adoptapp.healthservice.model.HealthHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthHistoryRepository extends JpaRepository<HealthHistory,Long> {
    List<HealthHistory> findByHealthIdOrderByChangedAtDesc(Long healthId);
}
