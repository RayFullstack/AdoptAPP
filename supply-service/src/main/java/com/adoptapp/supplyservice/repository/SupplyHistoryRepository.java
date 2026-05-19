package com.adoptapp.supplyservice.repository;

import com.adoptapp.supplyservice.model.SupplyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyHistoryRepository extends JpaRepository<SupplyHistory, Long> {
    List<SupplyHistory> findBySupplyIdOrderByCreatedAtDesc(Long supplyId);
}
