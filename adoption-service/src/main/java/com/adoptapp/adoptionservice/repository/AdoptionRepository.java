package com.adoptapp.adoptionservice.repository;

import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {

    List<Adoption> findByStatus(AdoptionStatus status);

    List<Adoption> findByUserIdAndStatusIn(Long userId, List<AdoptionStatus> statuses);

   boolean existsByPetIdAndStatusIn(Long petId, List<AdoptionStatus> statuses);

   boolean existsByPetIdAndStatusInAndIdNot(Long petId, List<AdoptionStatus> statuses, Long Id);

    List<Adoption> findByStatusIn(List<AdoptionStatus> statuses);
}
