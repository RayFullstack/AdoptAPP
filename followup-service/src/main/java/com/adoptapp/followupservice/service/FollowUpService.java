package com.adoptapp.followupservice.service;

import com.adoptapp.followupservice.dto.FollowUpCommand;
import com.adoptapp.followupservice.dto.FollowUpResult;
import com.adoptapp.followupservice.model.FollowUp;
import com.adoptapp.followupservice.model.FollowUpStatus;
import com.adoptapp.followupservice.repository.FollowUpRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FollowUpService {

    private final FollowUpRepository repository;

    public FollowUpService(FollowUpRepository repository) {
        this.repository = repository;
    }

    public List<FollowUpResult> getFollowUps() {

        return this.repository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<FollowUpResult> getFollowUps(String status) {

        return this.repository.findByStatus(
                        FollowUpStatus.valueOf(status.toUpperCase()))
                .stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<FollowUpResult> getById(Long id) {

        return this.repository.findById(id)
                .map(this::toResult);
    }

    public FollowUpResult create(FollowUpCommand command) {

        FollowUp followUp = new FollowUp();

        followUp.setAdopterName(command.adopterName());
        followUp.setPetName(command.petName());
        followUp.setVisitDate(command.visitDate());
        followUp.setComments(command.comments());
        followUp.setStatus(command.status());

        FollowUp saved = this.repository.save(followUp);

        return toResult(saved);
    }

    public Optional<FollowUpResult> updateById(
            Long id,
            FollowUpCommand command) {

        Optional<FollowUp> found = this.repository.findById(id);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        FollowUp followUp = found.get();

        followUp.setAdopterName(command.adopterName());
        followUp.setPetName(command.petName());
        followUp.setVisitDate(command.visitDate());
        followUp.setComments(command.comments());
        followUp.setStatus(command.status());

        FollowUp saved = this.repository.save(followUp);

        return Optional.of(toResult(saved));
    }

    public boolean deleteById(Long id) {

        if (!this.repository.existsById(id)) {
            return false;
        }

        this.repository.deleteById(id);

        return true;
    }

    private FollowUpResult toResult(FollowUp followUp) {

        return new FollowUpResult(
                followUp.getId(),
                followUp.getAdopterName(),
                followUp.getPetName(),
                followUp.getVisitDate(),
                followUp.getComments(),
                followUp.getStatus()
        );
    }
}