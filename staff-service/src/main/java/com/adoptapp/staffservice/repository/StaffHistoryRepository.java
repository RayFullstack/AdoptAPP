package com.adoptapp.staffservice.repository;

import com.adoptapp.staffservice.model.StaffHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffHistoryRepository extends JpaRepository<StaffHistory, Long> {

    List<StaffHistory> findByStaffIdOrderByChangedAtDesc(Long staffId);
}
