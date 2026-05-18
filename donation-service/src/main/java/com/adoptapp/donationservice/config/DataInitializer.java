package com.adoptapp.donationservice.config;

import com.adoptapp.donationservice.model.Donation;
import com.adoptapp.donationservice.model.DonationStatus;
import com.adoptapp.donationservice.repository.DonationRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("h2")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(DonationRepository repository) {

        return args -> {

            repository.save(new Donation(
                    null,
                    "Jacqueline Perez",
                    50000.0,
                    "Donación de alimentos",
                    DonationStatus.COMPLETED,
                    1L,
                    1L,
                    null,
                    null
            ));

            repository.save(new Donation(
                    null,
                    "Carlos Soto",
                    25000.0,
                    "Medicamentos para mascotas",
                    DonationStatus.PENDING,
                    2L,
                    1L,
                    null,
                    null
            ));

            repository.save(new Donation(
                    null,
                    "María López",
                    10000.0,
                    "Ayuda voluntaria",
                    DonationStatus.CANCELLED,
                    3L,
                    2L,
                    null,
                    null
            ));
        };
    }
}