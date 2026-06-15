package com.adoptapp.healthservice.repository;

import com.adoptapp.healthservice.model.Health;
import com.adoptapp.healthservice.model.HealthStatus;
import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface HealthRepository extends JpaRepository<Health,Long> {
    List<Health> findByVaccinationStatus(VaccinationStatus status);

    List<Health> findBySterilizationStatus(SterilizationStatus status);

    List<Health> findByStatus(HealthStatus status);

    List<Health> findByVaccinationStatusAndStatus(VaccinationStatus vaccinationStatus, HealthStatus status);

    List<Health> findBySterilizationStatusAndStatus(SterilizationStatus sterilizationStatus, HealthStatus status);

    Optional<Health> findByPetId(Long petId);

    Optional<Health> findByPetIdAndStatus(Long petId, HealthStatus status);

    boolean existsByPetId(Long petId);

    boolean existsByPetIdAndStatus(Long petId, HealthStatus status);

    boolean existsByPetIdAndIdNot(Long petId, Long id);

    boolean existsByPetIdAndStatusAndIdNot(Long petId, HealthStatus status, Long id);
}
