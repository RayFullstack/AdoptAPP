package com.adoptapp.donationservice.config;

import com.adoptapp.donationservice.model.Donation;
import com.adoptapp.donationservice.model.DonationStatus;
import com.adoptapp.donationservice.repository.DonationRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(DonationRepository repository) {

        return args -> {

            repository.save(new Donation(
                    null,
                    "Jacqueline Perez",
                    50000.0,
                    "Donación de alimentos",
                    DonationStatus.COMPLETED
            ));

            repository.save(new Donation(
                    null,
                    "Carlos Soto",
                    25000.0,
                    "Medicamentos para mascotas",
                    DonationStatus.PENDING
            ));

            repository.save(new Donation(
                    null,
                    "María López",
                    10000.0,
                    "Ayuda voluntaria",
                    DonationStatus.CANCELLED
            ));
        };
    }
}