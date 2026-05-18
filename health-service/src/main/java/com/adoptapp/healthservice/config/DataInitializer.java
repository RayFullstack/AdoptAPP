package com.adoptapp.healthservice.config;

import com.adoptapp.healthservice.model.Health;
import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import com.adoptapp.healthservice.repository.HealthRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("h2")
public class DataInitializer implements CommandLineRunner {

    private final HealthRepository healthRepository;

    public DataInitializer(HealthRepository healthRepository) {
        this.healthRepository = healthRepository;
    }

    @Override
    public void run(String... args) {
        if (healthRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            Health h1 = new Health();
            h1.setUserId(1L);
            h1.setPetId(1L);
            h1.setVaccinationStatus(VaccinationStatus.VACCINATED);
            h1.setSterilizationStatus(SterilizationStatus.STERILIZED);
            h1.setDiseases("Ninguna");
            h1.setCreatedAt(now);
            healthRepository.save(h1);

            Health h2 = new Health();
            h2.setUserId(2L);
            h2.setPetId(2L);
            h2.setVaccinationStatus(VaccinationStatus.NOT_VACCINATED);
            h2.setSterilizationStatus(SterilizationStatus.NOT_STERILIZED);
            h2.setDiseases("Leishmaniosis");
            h2.setCreatedAt(now);
            healthRepository.save(h2);

            Health h3 = new Health();
            h3.setUserId(3L);
            h3.setPetId(3L);
            h3.setVaccinationStatus(VaccinationStatus.VACCINATED);
            h3.setSterilizationStatus(SterilizationStatus.STERILIZED);
            h3.setDiseases("Ninguna");
            h3.setCreatedAt(now);
            healthRepository.save(h3);
        }
    }
}
