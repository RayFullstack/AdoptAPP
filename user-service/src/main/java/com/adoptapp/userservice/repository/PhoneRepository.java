package com.adoptapp.userservice.repository;

import com.adoptapp.userservice.model.UserPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PhoneRepository extends JpaRepository<UserPhone, Long> {
    boolean existsByNumberIgnoreCase(String number);

    Optional<UserPhone> findByNumberIgnoreCase(String number);
}
