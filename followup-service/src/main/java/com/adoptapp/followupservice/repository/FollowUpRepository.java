package com.adoptapp.followupservice.repository;

import com.adoptapp.followupservice.model.FollowUp;
import com.adoptapp.followupservice.model.FollowUpStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowUpRepository
        extends JpaRepository<FollowUp, Long> {

    List<FollowUp> findByStatus(FollowUpStatus status);
}
