package com.adoptapp.healthservice.repository;

import com.adoptapp.healthservice.model.Health;
import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthRepository extends JpaRepository<Health,Long> {
    List<Health> findByVaccinationStatus(VaccinationStatus status);

    List<Health> findBySterilizationStatus(SterilizationStatus status);

    List<Health> findAllByOrderByCreatedAtAsc();
}
