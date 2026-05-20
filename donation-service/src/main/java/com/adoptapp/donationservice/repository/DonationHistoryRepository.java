package com.adoptapp.donationservice.repository;

import com.adoptapp.donationservice.model.DonationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {

    List<DonationHistory> findByDonationIdOrderByChangedAtDesc(Long donationId);

    void deleteByDonationId(Long donationId);
}
