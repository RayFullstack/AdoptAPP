package com.adoptapp.adoptionservice.config;

import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import com.adoptapp.adoptionservice.repository.AdoptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("h2")
public class DataInitializer implements CommandLineRunner {

    private final AdoptionRepository adoptionRepository;

    public DataInitializer(AdoptionRepository adoptionRepository) {
        this.adoptionRepository = adoptionRepository;
    }

    @Override
    public void run(String... args) {
        if (adoptionRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            Adoption a1 = new Adoption();
            a1.setUserId(1L);
            a1.setPetId(1L);
            a1.setStatus(AdoptionStatus.PENDING);
            a1.setCreatedAt(now);
            adoptionRepository.save(a1);

            Adoption a2 = new Adoption();
            a2.setUserId(2L);
            a2.setPetId(2L);
            a2.setStatus(AdoptionStatus.APPROVED);
            a2.setCreatedAt(now);
            adoptionRepository.save(a2);

            Adoption a3 = new Adoption();
            a3.setUserId(3L);
            a3.setPetId(3L);
            a3.setStatus(AdoptionStatus.REJECTED);
            a3.setCreatedAt(now);
            adoptionRepository.save(a3);
        }
    }
}
