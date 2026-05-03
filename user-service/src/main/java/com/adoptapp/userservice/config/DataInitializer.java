package com.adoptapp.userservice.config;

import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
    public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;

        public DataInitializer(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public void run(String... args) throws Exception {
            if (userRepository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();
                LocalDate estimated = LocalDate.now().plusDays(5);

                User u1 = new User();
                u1.setUsername("LisaS");
                u1.setName("Lisa");
                u1.setSurname("Simpson");
                u1.setPhone("956443231");
                u1.setAddress("Av. Siempreviva 123");
                u1.setEmail("lsimpson@mail.com");
                u1.setStatus("ACTIVE");
                u1.setCreatedAt(now);
                userRepository.save(u1);

                User u2 = new User();
                u2.setUsername("BortS");
                u2.setName("Bort");
                u2.setSurname("Simpson");
                u2.setPhone("956444431");
                u2.setAddress("Av. Siempreviva 123");
                u2.setEmail("bsimpson@mail.com");
                u2.setStatus("ACTIVE");
                u2.setCreatedAt(now);
                userRepository.save(u2);

                User u3 = new User();
                u3.setUsername("homerS");
                u3.setName("Homer");
                u3.setSurname("Simpson");
                u3.setPhone("958564431");
                u3.setAddress("Av. Siempreviva 123");
                u3.setEmail("homosimp@mail.com");
                u3.setStatus("ACTIVE");
                u3.setCreatedAt(now);
                userRepository.save(u3);
            }
        }
}
