package com.adoptapp.followupservice.repository;

import com.adoptapp.followupservice.model.FollowUpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowUpHistoryRepository extends JpaRepository<FollowUpHistory, Long> {

    List<FollowUpHistory> findByFollowUpIdOrderByChangedAtDesc(Long followUpId);
}
