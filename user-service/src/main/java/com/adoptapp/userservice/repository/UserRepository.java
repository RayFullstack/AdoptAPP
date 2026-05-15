package com.adoptapp.userservice.repository;

import com.adoptapp.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByStatusIgnoreCase(String status);

    List<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByCreatedAtAsc();
}
