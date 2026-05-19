package com.adoptapp.healthservice.service;

import com.adoptapp.healthservice.client.*;
import com.adoptapp.healthservice.dto.*;
import com.adoptapp.healthservice.model.Health;
import com.adoptapp.healthservice.model.HealthHistory;
import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import com.adoptapp.healthservice.repository.HealthHistoryRepository;
import com.adoptapp.healthservice.repository.HealthRepository;
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

public class HealthService {
    private final HealthHistoryRepository healthHistoryRepository;
    private final HealthRepository healthRepository;
    private final PetServiceClient petServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final UserServiceClient userServiceClient;

    public HealthService(HealthRepository healthRepository,
                   HealthHistoryRepository healthHistoryRepository,
                   PetServiceClient petServiceClient,
                   NotificationServiceClient notificationServiceClient,
                   UserServiceClient userServiceClient) {
        this.healthRepository = healthRepository;
        this.healthHistoryRepository = healthHistoryRepository;
        this.petServiceClient = petServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.userServiceClient = userServiceClient;
    }

    public List<HealthResult> getHealth() {
        return this.healthRepository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<HealthResult> getVax(String vaccinationStatus) {
        VaccinationStatus vax = VaccinationStatus.valueOf(vaccinationStatus.toUpperCase());
        return this.healthRepository.findByVaccinationStatus(vax).stream()
                .map(this::toResult)
                .toList();
    }

    public List<HealthResult> getSter(String sterilizationStatus) {
        SterilizationStatus ster = SterilizationStatus.valueOf(sterilizationStatus.toUpperCase());
        return this.healthRepository.findBySterilizationStatus(ster).stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<HealthResult> getById(Long id) {
        return this.healthRepository.findById(id)
                .map(this::toResult);
    }

    @Transactional
    public HealthResult create(HealthCommand command) {
        log.info("Creando ficha clinica: userId={}, petId={}", command.userId(), command.petId());

        ResponseEntity<PetResponse> petResponse = petServiceClient.getPetById(command.petId());
        if (!petResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Mascota no encontrada: ID={}", command.petId());
            throw new IllegalArgumentException("La mascota con ID " + command.petId() + " no existe");
        }

        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Usuario no encontrad@: ID={}", command.userId());
            throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
        }

        Health health = new Health();
        health.setUserId(command.userId());
        health.setPetId(command.petId());
        health.setDiseases(command.diseases());
        health.setSterilizationStatus(command.sterilizationStatus());
        health.setVaccinationStatus(command.vaccinationStatus());
        try {
            Health saved = this.healthRepository.save(health);
            recordHistory(saved.getId(), "HEALTH_CHECK_CREATED",
                    "Ficha creada: mascota " + command.petId() + " por usuario " + command.userId(),
                    command.userId(),
                    null, command.sterilizationStatus().name(),
                    null, command.vaccinationStatus().name(),
                    null, command.diseases());

            String email = userResponse.getBody().email();
            sendNotification(command.userId(), email,"La ficha clinica de la mascota " + command.petId()
                    + " ha sido creada por el usuario " + command.userId(), "HEALTH_CHECK_CREATED");

            log.info("Ficha clínica creada exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear ficha clínica", e);
            throw e;
        }
    }


    private void sendNotification(Long userId, String recipient, String message, String typeName) {
        try {
            NotificationRequest request = new NotificationRequest(userId, recipient, message, typeName, "SENT");
            notificationServiceClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a {}: {}", recipient, e.getMessage());
        }
    }

    private HealthHistoryResponse toHistoryResponse(HealthHistory history) {
        return new HealthHistoryResponse(
                history.getHealth().getId(),
                history.getPreviousSterilizationStatus(),
                history.getNewSterilizationStatus(),
                history.getPreviousVaccinationStatus(),
                history.getNewVaccinationStatus(),
                history.getPreviousDisease(),
                history.getNewDisease(),
                history.getAction(),
                history.getChangedAt(),
                history.getComment(),
                history.getChangedByUserId()
        );
    }
    private HealthResult toResult(Health health) {
        return new HealthResult(
                health.getId(),
                health.getUserId(),
                health.getPetId(),
                health.getVaccinationStatus(),
                health.getSterilizationStatus(),
                health.getDiseases(),
                health.getCreatedAt(),
                health.getUpdatedAt()
        );
    }


    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando la ficha clinica: ID={}", id);

        Optional<Health> found = this.healthRepository.findById(id);
        if (found.isEmpty()) {
            log.warn("Ficha a eliminar no encontrada: ID={}", id);
            return false;
        }

        Health health = found.get();

        String email = null;
        try {
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(health.getUserId());
            if (userResponse.getStatusCode().is2xxSuccessful()) {
                email = userResponse.getBody().email();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener email del usuario {} para notificación", health.getUserId());
        }

        VaccinationStatus delVax = health.getVaccinationStatus();
        SterilizationStatus delSter = health.getSterilizationStatus();
        String delDiseases = health.getDiseases();
        recordHistory(id, "DELETED",
                "Ficha eliminada: mascota " + health.getPetId() + ", usuario " + health.getUserId(),
                health.getUserId(),
                delSter != null ? delSter.name() : null, null,
                delVax != null ? delVax.name() : null, null,
                delDiseases, null);

        if (email != null) {
            sendNotification(health.getUserId(), email, "La ficha " + id + " ha sido eliminada", "HEALTH_ALERT");
        }

        try {
            this.healthRepository.deleteById(id);
            log.info("Ficha eliminada exitosamente: ID={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar ficha: ID={}", id, e);
            throw e;
        }
    }

    @Transactional
    public Optional<HealthResult> updateById(Long id, HealthCommand command) {
        log.info("Actualizando ficha: ID={}", id);

        Optional<Health> found = this.healthRepository.findById(id);
        if (found.isEmpty()) {
            log.warn("Ficha no encontrada: ID={}", id);
            return Optional.empty();
        }

        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Usuario no encontrado: ID={}", command.userId());
            throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
        }

        ResponseEntity<PetResponse> petResponse = petServiceClient.getPetById(command.petId());
        if (!petResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Mascota no encontrada: ID={}", command.petId());
            throw new IllegalArgumentException("La mascota con ID " + command.petId() + " no existe");
        }

        Health toUpdate = found.get();
        VaccinationStatus prevVax = toUpdate.getVaccinationStatus();
        SterilizationStatus prevSter = toUpdate.getSterilizationStatus();
        String prevDiseases = toUpdate.getDiseases();

        toUpdate.setUserId(command.userId());
        toUpdate.setPetId(command.petId());
        toUpdate.setVaccinationStatus(command.vaccinationStatus());
        toUpdate.setSterilizationStatus(command.sterilizationStatus());
        toUpdate.setDiseases(command.diseases());

        try {
            Health updated = this.healthRepository.save(toUpdate);

            String cambios = "";
            if (!Objects.equals(prevVax, updated.getVaccinationStatus()))
                cambios += "vacunación: " + prevVax + "→" + updated.getVaccinationStatus() + ", ";
            if (!Objects.equals(prevSter, updated.getSterilizationStatus()))
                cambios += "esterilización: " + prevSter + "→" + updated.getSterilizationStatus() + ", ";
            if (!Objects.equals(prevDiseases, updated.getDiseases()))
                cambios += "enfermedades actualizadas, ";

            recordHistory(id, "UPDATED",
                    "Ficha mascota " + command.petId() + " modificada por usuario " + command.userId() +
                            ". Cambios: " + cambios,
                    command.userId(),
                    prevSter != null ? prevSter.name() : null, command.sterilizationStatus().name(),
                    prevVax != null ? prevVax.name() : null, command.vaccinationStatus().name(),
                    prevDiseases, command.diseases());

            String email = userResponse.getBody().email();
            sendNotification(command.userId(), email,
                    "Ficha " + id + " actualizada: " + cambios, "HEALTH_CHECK_UPDATED");

            log.info("Ficha clínica actualizada exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (Exception e) {
            log.error("Error al actualizar ficha clínica: ID={}", id, e);
            throw e;
        }
    }

    public List<HealthHistoryResponse> getHistory(Long healthId) {
        return healthHistoryRepository.findByHealthIdOrderByChangedAtDesc(healthId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void recordHistory(Long healthId, String action, String description, Long changedByUserId,
                                String prevSter, String newSter,
                                String prevVax, String newVax,
                                String prevDisease, String newDisease) {
        HealthHistory history = new HealthHistory();
        healthRepository.findById(healthId).ifPresent(history::setHealth);
        history.setAction(action);
        history.setComment(description);
        history.setChangedByUserId(changedByUserId);
        history.setPreviousSterilizationStatus(prevSter);
        history.setNewSterilizationStatus(newSter);
        history.setPreviousVaccinationStatus(prevVax);
        history.setNewVaccinationStatus(newVax);
        history.setPreviousDisease(prevDisease);
        history.setNewDisease(newDisease);
        history.setChangedAt(LocalDateTime.now());
        healthHistoryRepository.save(history);
    }

}
