package com.adoptapp.shelterservice.config;

import com.adoptapp.shelterservice.model.Shelter;
import com.adoptapp.shelterservice.model.ShelterStatus;
import com.adoptapp.shelterservice.repository.ShelterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
public class DataInitializer implements CommandLineRunner {

    private final ShelterRepository shelterRepository;

    public DataInitializer(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    @Override
    public void run(String... args) {
        if (shelterRepository.count() == 0) {
            Shelter s1 = new Shelter();
            s1.setName("Patitas Felices");
            s1.setEmail("contacto@patitasfelices.org");
            s1.setPhone("+56912345678");
            s1.setDescription("Refugio dedicado al rescate y adopción de perros y gatos en la región metropolitana.");
            s1.setStatus(ShelterStatus.ACTIVE);
            s1.setActive(true);
            shelterRepository.save(s1);

            Shelter s2 = new Shelter();
            s2.setName("Huella Animal");
            s2.setEmail("info@huellaanimal.cl");
            s2.setPhone("+56987654321");
            s2.setDescription("Centro de acogida temporal para animales abandonados. Contamos con atención veterinaria.");
            s2.setStatus(ShelterStatus.ACTIVE);
            s2.setActive(true);
            shelterRepository.save(s2);

            Shelter s3 = new Shelter();
            s3.setName("Nuevo Amanecer");
            s3.setEmail("rescue@nuevoamanecer.org");
            s3.setPhone("+56956781234");
            s3.setDescription("Refugio especializado en rehabilitación de animales maltratados.");
            s3.setStatus(ShelterStatus.INACTIVE);
            s3.setActive(false);
            shelterRepository.save(s3);
        }
    }
}
