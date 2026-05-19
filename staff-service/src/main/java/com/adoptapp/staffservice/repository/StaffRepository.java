package com.adoptapp.staffservice.repository;

import com.adoptapp.staffservice.model.Staff;
import com.adoptapp.staffservice.model.StaffStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findByStatus(StaffStatus status);

    List<Staff> findByShelterId(Long shelterId);

    List<Staff> findByPosition(String position);
}
