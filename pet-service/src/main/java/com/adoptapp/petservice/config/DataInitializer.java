package com.adoptapp.petservice.config;

import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetStatus;
import com.adoptapp.petservice.repository.PetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
public class DataInitializer implements CommandLineRunner {

    private final PetRepository petRepository;

    public DataInitializer(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (petRepository.count() == 0) {
            Pet p1 = new Pet();
            p1.setName("Yoni");
            p1.setAge(7);
            p1.setColor("Cafe");
            p1.setFosterId(1L);
            p1.setPersonality("Arisco");
            p1.setSpecies("Perro");
            p1.setSize("Grande");
            p1.setRace("Doberman");
            p1.setStatus(PetStatus.AVAILABLE);
            p1.setVaccinated(true);
            p1.setSterilized(false);
            p1.setDiseases("Ninguna");
            petRepository.save(p1);

            Pet p2 = new Pet();
            p2.setName("Loki");
            p2.setAge(4);
            p2.setColor("Negro");
            p2.setFosterId(1L);
            p2.setPersonality("Serena");
            p2.setSpecies("Gato");
            p2.setSize("Mediano");
            p2.setRace("Domestico pelo largo");
            p2.setStatus(PetStatus.AVAILABLE);
            p2.setVaccinated(false);
            p2.setSterilized(false);
            p2.setDiseases("NO SANO");
            petRepository.save(p2);

            Pet p3 = new Pet();
            p3.setName("Oso");
            p3.setAge(2);
            p3.setColor("Crema");
            p3.setFosterId(1L);
            p3.setPersonality("Regalon");
            p3.setSpecies("Perro");
            p3.setSize("Mediano");
            p3.setRace("Cocker");
            p3.setStatus(PetStatus.NOT_AVAILABLE);
            p3.setVaccinated(true);
            p3.setSterilized(false);
            p3.setDiseases("Ninguna");
            petRepository.save(p3);
        }
    }
}
