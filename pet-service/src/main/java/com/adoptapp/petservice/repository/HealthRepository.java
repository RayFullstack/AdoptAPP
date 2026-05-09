package com.adoptapp.petservice.repository;

import com.adoptapp.petservice.model.PetHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthRepository extends JpaRepository<PetHealth, Long> {

    boolean existsByDiseasesIgnoreCase(String diseases);

    List<PetHealth> findByVaccinated(Boolean vaccinated);

    List<PetHealth> findBySterilized(Boolean sterilized);

    List<PetHealth> findByVaccinatedFalse();

    List<PetHealth> findBySterilizedFalse();
}
