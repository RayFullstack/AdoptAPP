package com.adoptapp.petservice.config;

import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetHealth;
import com.adoptapp.petservice.model.PetStatus;
import com.adoptapp.petservice.repository.HealthRepository;
import com.adoptapp.petservice.repository.PetRepository;
import com.adoptapp.petservice.repository.StatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PetRepository petRepository;
    private final StatusRepository statusRepository;
    private final HealthRepository  healthRepository;

    public DataInitializer(PetRepository petRepository,
                           StatusRepository statusRepository,
                           HealthRepository healthRepository) {
        this.petRepository = petRepository;
        this.statusRepository = statusRepository;
        this.healthRepository = healthRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (petRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            PetStatus available = new PetStatus();
            available.setName("AVAILABLE");

            PetStatus notAvailable = new PetStatus();
            notAvailable.setName("NOT AVAILABLE");

            PetHealth healthy = new PetHealth();
            healthy.setVaccinated(true);
            healthy.setSterilized(false);
            healthy.setDiseases("Ninguna");

            PetHealth notHealthy = new PetHealth();
            healthy.setVaccinated(false);
            healthy.setSterilized(false);
            healthy.setDiseases("NO SANO");



            Pet p1 = new Pet();
            p1.setName("Yoni");
            p1.setAge(7);
            p1.setColor("Cafe");
            p1.setFosterId(1L);
            p1.setPersonality("Arisco");
            p1.setSpecies("Perro");
            p1.setSize("Grande");
            p1.setRace("Doberman");
            p1.setStatus(available);
            p1.setHealth(healthy);
            petRepository.save(p1);


            Pet p2 = new Pet();
            p2.setName("Loki");
            p2.setAge(4);
            p2.setColor("Negro");
            p2.setStatus(available);
            p2.setFosterId(1L);
            p2.setPersonality("Serena");
            p2.setHealth(healthy);
            p2.setSpecies("Gato");
            p2.setSize("Mediano");
            p2.setRace("Domestico pelo largo");
            p2.setStatus(available);
            p2.setHealth(notHealthy);
            petRepository.save(p2);

            Pet p3 = new Pet();
            p3.setName("Oso");
            p3.setAge(2);
            p3.setColor("Crema");
            p3.setStatus(available);
            p3.setFosterId(1L);
            p3.setPersonality("Regalon");
            p3.setHealth(healthy);
            p3.setSpecies("Perro");
            p3.setSize("Mediano");
            p3.setRace("Cocker");
            p3.setStatus(notAvailable);
            p3.setHealth(healthy);
            petRepository.save(p3);
        }
    }
}

