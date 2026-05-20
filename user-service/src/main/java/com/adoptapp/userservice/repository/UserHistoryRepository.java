package com.adoptapp.userservice.repository;

import com.adoptapp.userservice.model.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    List<UserHistory> findByUserIdOrderByChangedAtDesc(Long userId);
}
