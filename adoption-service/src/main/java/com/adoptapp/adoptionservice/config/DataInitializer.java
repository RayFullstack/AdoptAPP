package com.adoptapp.adoptionservice.config;

import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.repository.AdoptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AdoptionRepository adoptionRepository;

    public DataInitializer(AdoptionRepository adoptionRepository) {
        this.adoptionRepository = adoptionRepository;
    }

    @Override
    public void run(String... args) {

        if (adoptionRepository.count() == 0) {

            Adoption a1 = new Adoption();

            a1.setUserId(1L);
            a1.setPetId(1L);
            a1.setStatus("PENDING");

            adoptionRepository.save(a1);
        }
    }
}

