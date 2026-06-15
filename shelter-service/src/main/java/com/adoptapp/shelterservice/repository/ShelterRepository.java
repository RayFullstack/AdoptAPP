package com.adoptapp.shelterservice.repository;

import com.adoptapp.shelterservice.model.Shelter;
import com.adoptapp.shelterservice.model.ShelterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {

    List<Shelter> findByStatus(ShelterStatus status);

    List<Shelter> findByStatusNot(ShelterStatus status);
}
