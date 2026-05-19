package com.adoptapp.shelterservice.service;

import com.adoptapp.shelterservice.dto.ShelterHistoryResponse;
import com.adoptapp.shelterservice.model.Shelter;
import com.adoptapp.shelterservice.model.ShelterHistory;
import com.adoptapp.shelterservice.repository.ShelterHistoryRepository;
import com.adoptapp.shelterservice.repository.ShelterRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShelterHistoryService {

    private final ShelterHistoryRepository repository;
    private final ShelterRepository shelterRepository;

    public ShelterHistoryService(ShelterHistoryRepository repository,
                                  ShelterRepository shelterRepository) {
        this.repository = repository;
        this.shelterRepository = shelterRepository;
    }

    public List<ShelterHistoryResponse> getHistory(Long shelterId) {
        return repository.findByShelterIdOrderByChangedAtDesc(shelterId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void recordHistory(Long shelterId, String action, String comment, Long changedByUserId,
                               String previousName, String newName,
                               String previousEmail, String newEmail,
                               String previousPhone, String newPhone,
                               String previousDescription, String newDescription,
                               String previousStatus, String newStatus,
                               Boolean previousActive, Boolean newActive) {
        ShelterHistory history = new ShelterHistory();
        shelterRepository.findById(shelterId).ifPresent(history::setShelter);
        history.setAction(action);
        history.setComment(comment);
        history.setChangedByUserId(changedByUserId);
        history.setPreviousName(previousName);
        history.setNewName(newName);
        history.setPreviousEmail(previousEmail);
        history.setNewEmail(newEmail);
        history.setPreviousPhone(previousPhone);
        history.setNewPhone(newPhone);
        history.setPreviousDescription(previousDescription);
        history.setNewDescription(newDescription);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setPreviousActive(previousActive);
        history.setNewActive(newActive);
        history.setChangedAt(LocalDateTime.now());
        repository.save(history);
    }

    private ShelterHistoryResponse toResponse(ShelterHistory history) {
        return new ShelterHistoryResponse(
                history.getShelter().getId(),
                history.getAction(),
                history.getPreviousName(),
                history.getNewName(),
                history.getPreviousEmail(),
                history.getNewEmail(),
                history.getPreviousPhone(),
                history.getNewPhone(),
                history.getPreviousDescription(),
                history.getNewDescription(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getPreviousActive(),
                history.getNewActive(),
                history.getComment(),
                history.getChangedByUserId(),
                history.getChangedAt()
        );
    }
}
