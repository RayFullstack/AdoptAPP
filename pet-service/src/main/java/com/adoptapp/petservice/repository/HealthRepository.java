package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.PetHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HealthRepository extends JpaRepository<PetHealth, Long> {

    boolean existsByDiseasesIgnoreCase(String diseases);

    List<PetHealth> findByVaccinated(Boolean vaccinated);

    List<PetHealth> findBySterilized(Boolean sterilized);

    List<PetHealth> findByVaccinatedFalse();

    List<PetHealth> findBySterilizedFalse();
}
