package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByStatusInOrderByCreatedAtAsc(List<PetStatus> statuses);

    List<Pet> findByStatus(PetStatus status);

    List<Pet> findByShelterIdAndStatusInOrderByCreatedAtAsc(Long shelterId, List<PetStatus> statuses);

    List<Pet> findByShelterIdAndStatus(Long shelterId, PetStatus status);
}
