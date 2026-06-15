package com.adoptapp.userservice.repository;

import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByStatusNotOrderByCreatedAtAsc(UserStatus status);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByCreatedAtAsc();
}
