package com.adoptapp.donationservice.repository;

import com.adoptapp.donationservice.model.DonationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {

    List<DonationHistory> findByDonationIdOrderByChangedAtDesc(Long donationId);
}
