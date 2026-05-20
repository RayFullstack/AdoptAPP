package com.adoptapp.donationservice.config;

import com.adoptapp.donationservice.model.Donation;
import com.adoptapp.donationservice.model.DonationStatus;
import com.adoptapp.donationservice.repository.DonationRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD

@Configuration
=======
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("h2")
>>>>>>> origin/camila-dev
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(DonationRepository repository) {

        return args -> {

            repository.save(new Donation(
                    null,
                    "Jacqueline Perez",
<<<<<<< HEAD
                    50000.0,
                    "Donación de alimentos",
                    DonationStatus.COMPLETED
=======
                    new BigDecimal("50000.00"),
                    "Donación de alimentos",
                    DonationStatus.COMPLETED,
                    1L,
                    1L,
                    null,
                    null
>>>>>>> origin/camila-dev
            ));

            repository.save(new Donation(
                    null,
                    "Carlos Soto",
<<<<<<< HEAD
                    25000.0,
                    "Medicamentos para mascotas",
                    DonationStatus.PENDING
=======
                    new BigDecimal("25000.00"),
                    "Medicamentos para mascotas",
                    DonationStatus.PENDING,
                    2L,
                    1L,
                    null,
                    null
>>>>>>> origin/camila-dev
            ));

            repository.save(new Donation(
                    null,
                    "María López",
<<<<<<< HEAD
                    10000.0,
                    "Ayuda voluntaria",
                    DonationStatus.CANCELLED
=======
                    new BigDecimal("10000.00"),
                    "Ayuda voluntaria",
                    DonationStatus.CANCELLED,
                    3L,
                    2L,
                    null,
                    null
>>>>>>> origin/camila-dev
            ));
        };
    }
}