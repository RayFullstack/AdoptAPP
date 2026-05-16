package com.adoptapp.donationservice.repository;

import com.adoptapp.donationservice.model.Donation;
import com.adoptapp.donationservice.model.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository
        extends JpaRepository<Donation, Long> {

    List<Donation> findByStatus(DonationStatus status);
}