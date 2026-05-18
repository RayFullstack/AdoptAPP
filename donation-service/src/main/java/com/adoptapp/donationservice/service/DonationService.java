package com.adoptapp.donationservice.service;

import com.adoptapp.donationservice.client.NotificationServiceClient;
import com.adoptapp.donationservice.client.ShelterServiceClient;
import com.adoptapp.donationservice.client.UserServiceClient;
import com.adoptapp.donationservice.dto.*;
import com.adoptapp.donationservice.model.Donation;
import com.adoptapp.donationservice.model.DonationHistory;
import com.adoptapp.donationservice.model.DonationStatus;
import com.adoptapp.donationservice.repository.DonationHistoryRepository;
import com.adoptapp.donationservice.repository.DonationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class DonationService {

    private final DonationRepository repository;
    private final DonationHistoryRepository historyRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ShelterServiceClient shelterServiceClient;

    public DonationService(DonationRepository repository,
                           DonationHistoryRepository historyRepository,
                           UserServiceClient userServiceClient,
                           NotificationServiceClient notificationServiceClient,
                           ShelterServiceClient shelterServiceClient) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.userServiceClient = userServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.shelterServiceClient = shelterServiceClient;
    }

    public List<DonationResult> getDonations() {
        return repository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<DonationResult> getDonations(String status) {
        DonationStatus donationStatus =
                DonationStatus.valueOf(status.toUpperCase());
        return repository.findByStatus(donationStatus).stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<DonationResult> getById(Long id) {
        return repository.findById(id)
                .map(this::toResult);
    }

    public List<DonationHistoryResponse> getHistory(Long donationId) {
        return historyRepository.findByDonationIdOrderByChangedAtDesc(donationId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public DonationResult create(DonationCommand command) {
        log.info("Creando donación: userId={}, shelterId={}", command.userId(), command.shelterId());

        ResponseEntity<ShelterResponse> shelterResponse = shelterServiceClient.getShelterById(command.shelterId());
        if (!shelterResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Refugio no encontrado: ID={}", command.shelterId());
            throw new IllegalArgumentException("El refugio con ID " + command.shelterId() + " no existe");
        }

        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Usuario no encontrado: ID={}", command.userId());
            throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
        }

        Donation donation = new Donation();
        donation.setDonorName(command.donorName());
        donation.setAmount(command.amount());
        donation.setDescription(command.description());
        donation.setStatus(command.status());
        donation.setShelterId(command.shelterId());
        donation.setUserId(command.userId());

        try {
            Donation saved = repository.save(donation);

            recordHistory(saved.getId(), "CREATED",
                    "Donación creada: " + command.donorName() + " - $" + command.amount(),
                    command.userId(),
                    null, command.status() != null ? command.status().name() : null,
                    null, command.amount());

            String email = userResponse.getBody().email();
            sendNotification(command.userId(), email,
                    "La donacion al refugio " + command.shelterId()
                            + " ha sido creada por el usuario " + command.userId(), "CREATED");

            log.info("Donación creada exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear donación", e);
            throw e;
        }
    }

    @Transactional
    public Optional<DonationResult> updateById(Long id, DonationCommand command) {
        log.info("Actualizando donación: ID={}", id);

        Optional<Donation> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Donación no encontrada: ID={}", id);
            return Optional.empty();
        }

        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Usuario no encontrado: ID={}", command.userId());
            throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
        }

        Donation toUpdate = found.get();
        DonationStatus prevStatus = toUpdate.getStatus();
        Double prevAmount = toUpdate.getAmount();

        toUpdate.setDonorName(command.donorName());
        toUpdate.setAmount(command.amount());
        toUpdate.setDescription(command.description());
        toUpdate.setStatus(command.status());

        try {
            Donation updated = repository.save(toUpdate);

            String cambios = "";
            if (!Objects.equals(prevStatus, updated.getStatus()))
                cambios += "estado: " + prevStatus + "→" + updated.getStatus() + ", ";
            if (!Objects.equals(prevAmount, updated.getAmount()))
                cambios += "monto: $" + prevAmount + "→$" + updated.getAmount() + ", ";

            recordHistory(id, "UPDATED",
                    "Donación modificada. Cambios: " + cambios,
                    command.userId(),
                    prevStatus != null ? prevStatus.name() : null,
                    command.status() != null ? command.status().name() : null,
                    prevAmount, command.amount());

            String email = userResponse.getBody().email();
            sendNotification(command.userId(), email,
                    "Donación " + id + " actualizada: " + cambios, "DONATION_UPDATED");

            log.info("Donación actualizada exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (Exception e) {
            log.error("Error al actualizar donación: ID={}", id, e);
            throw e;
        }
    }

    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando donación: ID={}", id);

        Optional<Donation> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Donación a eliminar no encontrada: ID={}", id);
            return false;
        }

        Donation donation = found.get();

        String email = null;
        try {
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(donation.getUserId());
            if (userResponse.getStatusCode().is2xxSuccessful()) {
                email = userResponse.getBody().email();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener email del usuario {} para notificación", donation.getUserId());
        }

        String delStatus = donation.getStatus() != null ? donation.getStatus().name() : null;
        Double delAmount = donation.getAmount();
        recordHistory(id, "DELETED",
                "Donación eliminada: " + donation.getDonorName() + " - $" + donation.getAmount(),
                donation.getUserId(),
                delStatus, null,
                delAmount, null);

        if (email != null) {
            sendNotification(donation.getUserId(), email,
                    "La donación " + id + " ha sido eliminada", "DELETED");
        }

        try {
            repository.deleteById(id);
            log.info("Donación eliminada exitosamente: ID={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar donación: ID={}", id, e);
            throw e;
        }
    }

    private void recordHistory(Long donationId, String action, String comment, Long changedByUserId,
                                String prevStatus, String newStatus,
                                Double prevAmount, Double newAmount) {
        DonationHistory history = new DonationHistory();
        history.setDonationId(donationId);
        history.setAction(action);
        history.setComment(comment);
        history.setChangedByUserId(changedByUserId);
        history.setPreviousStatus(prevStatus);
        history.setNewStatus(newStatus);
        history.setPreviousAmount(prevAmount);
        history.setNewAmount(newAmount);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private DonationHistoryResponse toHistoryResponse(DonationHistory history) {
        return new DonationHistoryResponse(
                history.getDonationId(),
                history.getAction(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getPreviousAmount(),
                history.getNewAmount(),
                history.getComment(),
                history.getChangedAt(),
                history.getChangedByUserId()
        );
    }

    private DonationResult toResult(Donation donation) {
        return new DonationResult(
                donation.getId(),
                donation.getDonorName(),
                donation.getAmount(),
                donation.getDescription(),
                donation.getStatus(),
                donation.getUserId(),
                donation.getShelterId(),
                donation.getCreatedAt(),
                donation.getUpdatedAt()
        );
    }

    private void sendNotification(Long userId, String recipient, String message, String typeName) {
        try {
            NotificationRequest request = new NotificationRequest(userId, recipient, message, typeName, "SENT");
            notificationServiceClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a {}: {}", recipient, e.getMessage());
        }
    }
}
