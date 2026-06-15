package com.adoptapp.healthservice.service;

import com.adoptapp.healthservice.client.*;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.healthservice.dto.*;
import com.adoptapp.healthservice.model.Health;
import com.adoptapp.healthservice.model.HealthHistory;
import com.adoptapp.healthservice.model.HealthStatus;
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
    private final StaffServiceClient staffServiceClient;

    public HealthService(HealthRepository healthRepository,
                   HealthHistoryRepository healthHistoryRepository,
                   PetServiceClient petServiceClient,
                   NotificationServiceClient notificationServiceClient,
                   UserServiceClient userServiceClient,
                   StaffServiceClient staffServiceClient) {
        this.healthRepository = healthRepository;
        this.healthHistoryRepository = healthHistoryRepository;
        this.petServiceClient = petServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.userServiceClient = userServiceClient;
        this.staffServiceClient = staffServiceClient;
    }

    public List<HealthResult> getHealth() {
        return this.healthRepository.findByStatus(HealthStatus.ACTIVE).stream()
                .map(this::toResult)
                .toList();
    }

    public List<HealthResult> getVax(String vaccinationStatus) {
        try {
            VaccinationStatus vax = VaccinationStatus.valueOf(vaccinationStatus.toUpperCase());
            return this.healthRepository.findByVaccinationStatusAndStatus(vax, HealthStatus.ACTIVE).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado de vacunación inválido: '{}'", vaccinationStatus);
            throw new IllegalArgumentException("Estado de vacunacion invalido: " + vaccinationStatus);
        }
    }

    public List<HealthResult> getSter(String sterilizationStatus) {
        try {
            SterilizationStatus ster = SterilizationStatus.valueOf(sterilizationStatus.toUpperCase());
            return this.healthRepository.findBySterilizationStatusAndStatus(ster, HealthStatus.ACTIVE).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado de esterilización inválido: '{}'", sterilizationStatus);
            throw new IllegalArgumentException("Estado de esterilizacion invalido: " + sterilizationStatus);
        }
    }

    public Optional<HealthResult> getById(Long id) {
        return this.healthRepository.findById(id)
                .filter(health -> health.getStatus() == HealthStatus.ACTIVE)
                .map(this::toResult);
    }

    public Optional<HealthResult> getByPetId(Long petId) {
        return this.healthRepository.findByPetIdAndStatus(petId, HealthStatus.ACTIVE)
                .map(this::toResult);
    }

    public Optional<HealthResult> getByIdIncludingDeleted(Long id) {
        return this.healthRepository.findById(id)
                .map(this::toResult);
    }

    public Long getUserIdByEmail(String email) {
        ResponseEntity<UserAuthResponse> response = userServiceClient.getUserAuthByEmail(email);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("Usuario autenticado no encontrado: " + email);
        }
        return response.getBody().id();
    }

    public Long getShelterIdForStaffUser(Long userId) {
        ResponseEntity<StaffResponse> response = staffServiceClient.getStaffByUserId(userId);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("El usuario no tiene staff activo asociado");
        }
        return response.getBody().shelterId();
    }

    public boolean petBelongsToShelter(Long petId, Long shelterId) {
        ResponseEntity<PetResponse> response = petServiceClient.getPetById(petId);
        return response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && shelterId.equals(response.getBody().shelterId());
    }

    @Transactional
    public HealthResult create(HealthCommand command) {
        log.info("Creando ficha clinica: userId={}, petId={}", command.userId(), command.petId());
        try {
            ResponseEntity<PetResponse> petResponse = petServiceClient.getPetById(command.petId());
            if (healthRepository.existsByPetIdAndStatus(command.petId(), HealthStatus.ACTIVE)) {
                throw new IllegalArgumentException("La mascota ya tiene una ficha de salud");
            }
            if (!petResponse.getStatusCode().is2xxSuccessful()) {
                log.warn("Mascota no encontrada: ID={}", command.petId());
                throw new IllegalArgumentException("La mascota con ID " + command.petId() + " no existe");
            }

            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                log.warn("Usuario no encontrado: ID={}", command.userId());
                throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
            }

            Health health = new Health();
            health.setUserId(command.userId());
            health.setPetId(command.petId());
            health.setDiseases(command.diseases());
            health.setSterilizationStatus(command.sterilizationStatus());
            health.setVaccinationStatus(command.vaccinationStatus());

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
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear ficha clínica: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al crear ficha clínica: no se pudo completar la validación");
        }
    }

    @Transactional
    public boolean deleteByPetId(Long petId) {
        log.info("Eliminando la ficha clinica por Id de la mascota: ID={}", petId);

        Optional<Health> found = this.healthRepository.findByPetIdAndStatus(petId, HealthStatus.ACTIVE);

        if (found.isEmpty()) {
            log.warn("Ficha a eliminar no encontrada para  petID: ID={}", petId);
            return false;
        }

        Health health = found.get();
        if (health.getStatus() == HealthStatus.DELETED) {
            log.warn("Ficha ya eliminada para petId={}", petId);
            return false;
        }

        String email = null;

        VaccinationStatus delVax = health.getVaccinationStatus();
        SterilizationStatus delSter = health.getSterilizationStatus();
        String delDiseases = health.getDiseases();
        recordHistory(health.getId(), "DELETED",
                "Ficha eliminada: mascota " + health.getPetId() + ", usuario " + health.getUserId(),
                health.getUserId(),
                delSter != null ? delSter.name() : null, null,
                delVax != null ? delVax.name() : null, null,
                delDiseases, null);

        try {
            var userResponse = userServiceClient.getUserById(health.getUserId());

            if (userResponse.getBody() != null) {
                email = userResponse.getBody().email();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener email del usuario ID={}: {}", health.getUserId(), e.getMessage());
        }
        if (email != null) {
            sendNotification(health.getUserId(), email, "La ficha " + petId + " ha sido eliminada", "HEALTH_ALERT");
        }

        try {
            health.setStatus(HealthStatus.DELETED);
            this.healthRepository.save(health);
            log.info("Ficha eliminada exitosamente: ID={}", petId);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar ficha: ID={}", petId, e);
            throw e;
        }
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
        if (health.getStatus() == HealthStatus.DELETED) {
            log.warn("Ficha ya eliminada: ID={}", id);
            return false;
        }

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
            health.setStatus(HealthStatus.DELETED);
            this.healthRepository.save(health);
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
        try {
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
            if (toUpdate.getStatus() == HealthStatus.DELETED) {
                throw new IllegalArgumentException("No se puede actualizar una ficha clinica eliminada");
            }

            if (!toUpdate.getPetId().equals(command.petId())) {
                throw new IllegalArgumentException("No se puede cambiar la mascota asociada a una ficha clinica");
            }

            if (!toUpdate.getUserId().equals(command.userId())) {
                throw new IllegalArgumentException("No se puede cambiar el usuario asociado a una ficha clinica");
            }


            VaccinationStatus prevVax = toUpdate.getVaccinationStatus();
            SterilizationStatus prevSter = toUpdate.getSterilizationStatus();
            String prevDiseases = toUpdate.getDiseases();

            toUpdate.setVaccinationStatus(command.vaccinationStatus());
            toUpdate.setSterilizationStatus(command.sterilizationStatus());
            toUpdate.setDiseases(command.diseases());

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
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar ficha clínica: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al actualizar ficha clínica: no se pudo completar la validación");
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

    private void sendNotification(Long userId, String recipient, String message, String typeName) {
        try {
            NotificationRequest request = new NotificationRequest(userId, null, recipient, message, typeName, "SENT");
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
                health.getStatus(),
                health.getCreatedAt(),
                health.getUpdatedAt()
        );
    }
}
