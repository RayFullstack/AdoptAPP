package com.adoptapp.adoptionservice.config;

import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.repository.AdoptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AdoptionRepository adoptionRepository;

    public DataInitializer(AdoptionRepository adoptionRepository) {this.adoptionRepository = adoptionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adoptionRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            Adoption a1 = new Adoption();
            a1.setUserId(command.userId());
            a1.setPetId(command.petId());
            a1.setStatus("PENDING");

                    }
    }
}


