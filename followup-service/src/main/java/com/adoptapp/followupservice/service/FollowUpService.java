package com.adoptapp.followupservice.service;

import com.adoptapp.followupservice.client.NotificationServiceClient;
import com.adoptapp.followupservice.client.PetServiceClient;
import com.adoptapp.followupservice.client.UserServiceClient;
import com.adoptapp.followupservice.dto.*;
import com.adoptapp.followupservice.model.FollowUp;
import com.adoptapp.followupservice.model.FollowUpHistory;
import com.adoptapp.followupservice.model.FollowUpStatus;
import com.adoptapp.followupservice.repository.FollowUpHistoryRepository;
import com.adoptapp.followupservice.repository.FollowUpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class FollowUpService {

    private final FollowUpRepository repository;
    private final FollowUpHistoryRepository historyRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final PetServiceClient petServiceClient;

    public FollowUpService(FollowUpRepository repository,
                            FollowUpHistoryRepository historyRepository,
                            UserServiceClient userServiceClient,
                            NotificationServiceClient notificationServiceClient,
                            PetServiceClient petServiceClient) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.userServiceClient = userServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.petServiceClient = petServiceClient;
    }

    public List<FollowUpResult> getFollowUps() {
        return repository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<FollowUpResult> getFollowUps(String status) {
        try {
            FollowUpStatus followUpStatus = FollowUpStatus.valueOf(status.toUpperCase());
            return repository.findByStatus(followUpStatus).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para seguimiento: '{}'", status);
            return List.of();
        }
    }

    public Optional<FollowUpResult> getById(Long id) {
        return repository.findById(id)
                .map(this::toResult);
    }

    public List<FollowUpHistoryResponse> getHistory(Long followUpId) {
        return historyRepository.findByFollowUpIdOrderByChangedAtDesc(followUpId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public FollowUpResult create(FollowUpCommand command) {
        log.info("Creando seguimiento: adopterName={}, petName={}", command.adopterName(), command.petName());

        FollowUp followUp = new FollowUp();
        followUp.setAdopterName(command.adopterName());
        followUp.setPetName(command.petName());
        followUp.setUserId(command.userId());
        followUp.setPetId(command.petId());
        followUp.setAdoptionId(command.adoptionId());
        followUp.setVisitDate(command.visitDate());
        followUp.setComments(command.comments());
        followUp.setStatus(command.status() != null ? command.status() : FollowUpStatus.PENDING);

        try {
            FollowUp saved = repository.save(followUp);

            recordHistory(saved.getId(), "CREATED",
                    "Seguimiento creado: " + command.adopterName() + " - " + command.petName(),
                    null,
                    null, saved.getStatus().name());

            sendNotification(saved.getId(), "Seguimiento creado para " + command.adopterName()
                    + " y mascota " + command.petName(), "FOLLOWUP_SCHEDULED");

            log.info("Seguimiento creado exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear seguimiento", e);
            throw e;
        }
    }

    @Transactional
    public Optional<FollowUpResult> updateById(Long id, FollowUpCommand command) {
        log.info("Actualizando seguimiento: ID={}", id);

        Optional<FollowUp> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Seguimiento no encontrado: ID={}", id);
            return Optional.empty();
        }

        FollowUp toUpdate = found.get();
        FollowUpStatus prevStatus = toUpdate.getStatus();

        toUpdate.setAdopterName(command.adopterName());
        toUpdate.setPetName(command.petName());
        if (command.userId() != null) toUpdate.setUserId(command.userId());
        if (command.petId() != null) toUpdate.setPetId(command.petId());
        if (command.adoptionId() != null) toUpdate.setAdoptionId(command.adoptionId());
        toUpdate.setVisitDate(command.visitDate());
        toUpdate.setComments(command.comments());
        if (command.status() != null) {
            toUpdate.setStatus(command.status());
        }

        try {
            FollowUp updated = repository.save(toUpdate);

            String cambios = "";
            if (!Objects.equals(prevStatus, updated.getStatus()))
                cambios += "estado: " + prevStatus + "→" + updated.getStatus();

            recordHistory(id, "UPDATED",
                    "Seguimiento modificado. Cambios: " + cambios,
                    prevStatus != null ? prevStatus.name() : null,
                    updated.getStatus() != null ? updated.getStatus().name() : null);

            sendNotification(id, "Seguimiento " + id + " actualizado: " + cambios, "FOLLOWUP_COMPLETED");

            log.info("Seguimiento actualizado exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (Exception e) {
            log.error("Error al actualizar seguimiento: ID={}", id, e);
            throw e;
        }
    }

    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando seguimiento: ID={}", id);

        Optional<FollowUp> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Seguimiento a eliminar no encontrado: ID={}", id);
            return false;
        }

        FollowUp followUp = found.get();
        String delStatus = followUp.getStatus() != null ? followUp.getStatus().name() : null;

        recordHistory(id, "DELETED",
                "Seguimiento eliminado: " + followUp.getAdopterName() + " - " + followUp.getPetName(),
                delStatus, null);

        sendNotification(id, "Seguimiento " + id + " ha sido eliminado", "FOLLOWUP_CANCELLED");

        try {
            repository.deleteById(id);
            log.info("Seguimiento eliminado exitosamente: ID={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar seguimiento: ID={}", id, e);
            throw e;
        }
    }

    private void recordHistory(Long followUpId, String action, String comment,
                                String previousStatus, String newStatus) {
        recordHistory(followUpId, action, comment, null, previousStatus, newStatus);
    }

    private void recordHistory(Long followUpId, String action, String comment, Long changedByUserId,
                                String previousStatus, String newStatus) {
        FollowUpHistory history = new FollowUpHistory();
        repository.findById(followUpId).ifPresent(history::setFollowUp);
        history.setAction(action);
        history.setComment(comment);
        history.setChangedByUserId(changedByUserId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void sendNotification(Long followUpId, String message, String typeName) {
        try {
            String recipient = "sistema@adoptapp.com";
            NotificationRequest request = new NotificationRequest(null, recipient, message, typeName, "SENT");
            notificationServiceClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion para seguimiento {}: {}", followUpId, e.getMessage());
        }
    }

    private FollowUpResult toResult(FollowUp followUp) {
        return new FollowUpResult(
                followUp.getId(),
                followUp.getAdopterName(),
                followUp.getPetName(),
                followUp.getUserId(),
                followUp.getPetId(),
                followUp.getAdoptionId(),
                followUp.getVisitDate(),
                followUp.getComments(),
                followUp.getStatus(),
                followUp.getCreatedAt(),
                followUp.getUpdatedAt()
        );
    }

    private FollowUpHistoryResponse toHistoryResponse(FollowUpHistory history) {
        return new FollowUpHistoryResponse(
                history.getFollowUp().getId(),
                history.getAction(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getComment(),
                history.getChangedByUserId(),
                history.getChangedAt()
        );
    }
}
