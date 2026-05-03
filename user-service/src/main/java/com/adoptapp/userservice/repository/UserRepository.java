package com.adoptapp.userservice.repository;

import com.adoptapp.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByStatusIgnoreCase(String status);

    List<User> findAllByOrderByCreatedAtAsc();
}
