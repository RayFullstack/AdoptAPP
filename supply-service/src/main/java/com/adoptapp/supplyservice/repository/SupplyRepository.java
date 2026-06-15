package com.adoptapp.supplyservice.repository;

import com.adoptapp.supplyservice.model.Supply;
import com.adoptapp.supplyservice.model.SupplyCategory;
import com.adoptapp.supplyservice.model.SupplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {
    List<Supply> findByShelterId(Long shelterId);
    List<Supply> findByStatus(SupplyStatus status);
    List<Supply> findByStatusNot(SupplyStatus status);
    List<Supply> findByCategory(SupplyCategory category);
    List<Supply> findByShelterIdAndCategory(Long shelterId, SupplyCategory category);
    List<Supply> findByShelterIdAndStatus(Long shelterId, SupplyStatus status);
    List<Supply> findByShelterIdAndStatusNot(Long shelterId, SupplyStatus status);
}
