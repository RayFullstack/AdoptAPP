package com.adoptapp.donationservice.service;

import com.adoptapp.donationservice.dto.DonationCommand;
import com.adoptapp.donationservice.dto.DonationResult;
import com.adoptapp.donationservice.model.Donation;
import com.adoptapp.donationservice.model.DonationStatus;
import com.adoptapp.donationservice.repository.DonationRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DonationService {

    private final DonationRepository repository;

    public DonationService(DonationRepository repository) {
        this.repository = repository;
    }

    public List<DonationResult> getDonations() {

        return repository.findAll()
                .stream()
                .map(this::toResult)
                .toList();
    }

    public List<DonationResult> getDonations(String status) {

        DonationStatus donationStatus =
                DonationStatus.valueOf(status.toUpperCase());

        return repository.findByStatus(donationStatus)
                .stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<DonationResult> getById(Long id) {

        return repository.findById(id)
                .map(this::toResult);
    }

    public DonationResult create(DonationCommand command) {

        Donation donation = new Donation(
                null,
                command.donorName(),
                command.amount(),
                command.description(),
                command.status()
        );

        Donation saved = repository.save(donation);

        return toResult(saved);
    }

    public Optional<DonationResult> updateById(
            Long id,
            DonationCommand command) {

        return repository.findById(id)
                .map(existing -> {

                    existing.setDonorName(command.donorName());
                    existing.setAmount(command.amount());
                    existing.setDescription(command.description());
                    existing.setStatus(command.status());

                    Donation updated = repository.save(existing);

                    return toResult(updated);
                });
    }

    public boolean deleteById(Long id) {

        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);

        return true;
    }

    private DonationResult toResult(Donation donation) {

        return new DonationResult(
                donation.getId(),
                donation.getDonorName(),
                donation.getAmount(),
                donation.getDescription(),
                donation.getStatus()
        );
    }
}
