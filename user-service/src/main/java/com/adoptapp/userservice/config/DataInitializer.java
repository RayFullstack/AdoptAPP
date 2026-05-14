package com.adoptapp.userservice.config;

import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.model.UserAddress;
import com.adoptapp.userservice.model.UserPhone;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("h2")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {

            LocalDateTime now = LocalDateTime.now();

            User u1 = new User();

            u1.setUsername("LisaS");
            u1.setName("Lisa");
            u1.setSurname("Simpson");
            u1.setEmail("lsimpson@mail.com");
            u1.setStatus(UserStatus.ACTIVE);
            u1.setCreatedAt(now);

            UserPhone phone1 = new UserPhone();
            phone1.setNumber("123456789");
            phone1.setUser(u1);

            u1.setPhone(phone1);

            UserAddress address1 = new UserAddress();

            address1.setCountry("Chile");
            address1.setCity("Santiago");
            address1.setStreet("Av. Siempreviva");
            address1.setHomeNumber("123");
            address1.setPostalCode("123456");
            address1.setType("HOME");
            address1.setPrimaryAddress(true);
            address1.setUser(u1);

            u1.setAddresses(List.of(address1));

            userRepository.save(u1);


            User u2 = new User();

            u2.setUsername("HomoSimp");
            u2.setName("Homero");
            u2.setSurname("Simpson");
            u2.setEmail("homerosimp@mail.com");
            u2.setStatus(UserStatus.ACTIVE);
            u2.setCreatedAt(now);

            UserPhone phone2 = new UserPhone();
            phone2.setNumber("954456881");
            phone2.setUser(u2);

            u2.setPhone(phone2);

            UserAddress address2 = new UserAddress();

            address2.setCountry("Chile");
            address2.setCity("Santiago");
            address2.setStreet("Av. Siempreviva");
            address2.setHomeNumber("456");
            address2.setPostalCode("345678");
            address2.setType("HOME");
            address2.setPrimaryAddress(true);
            address2.setUser(u2);

            u2.setAddresses(List.of(address2));

            userRepository.save(u2);


            User u3 = new User();

            u3.setUsername("StacyBakr");
            u3.setName("Stacy");
            u3.setSurname("Baker");
            u3.setEmail("fakemail123@mail.com");
            u3.setStatus(UserStatus.ACTIVE);
            u3.setCreatedAt(now);

            UserPhone phone3 = new UserPhone();

            phone3.setNumber("9564422331");
            phone3.setUser(u3);

            u3.setPhone(phone3);

            UserAddress address3 = new UserAddress();

            address3.setCountry("Chile");
            address3.setCity("Santiago");
            address3.setStreet("Av. Real");
            address3.setHomeNumber("7865");
            address3.setPostalCode("22222455");
            address3.setType("WORK");
            address3.setPrimaryAddress(true);

            address3.setUser(u3);

            u3.setAddresses(List.of(address3));

            userRepository.save(u3);
        }
    }
}
