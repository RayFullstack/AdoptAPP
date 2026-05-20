package com.adoptapp.adoptionservice.service;

import com.adoptapp.adoptionservice.client.FollowUpServiceClient;
import com.adoptapp.adoptionservice.client.PetNotificationClient;
import com.adoptapp.adoptionservice.client.PetServiceClient;
import com.adoptapp.adoptionservice.client.ShelterServiceClient;
import com.adoptapp.adoptionservice.client.UserNotificationClient;
import com.adoptapp.adoptionservice.client.UserServiceClient;
import com.adoptapp.adoptionservice.dto.*;
import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.model.AdoptionHistory;
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import com.adoptapp.adoptionservice.repository.AdoptionHistoryRepository;
import com.adoptapp.adoptionservice.repository.AdoptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AdoptionService {

    private final AdoptionRepository repository;
    private final AdoptionHistoryRepository historyRepository;
    private final UserServiceClient userServiceClient;
    private final PetServiceClient petServiceClient;
    private final ShelterServiceClient shelterServiceClient;
    private final FollowUpServiceClient followUpServiceClient;
    private final UserNotificationClient userNotificationClient;
    private final PetNotificationClient petNotificationClient;

    public AdoptionService(AdoptionRepository repository, AdoptionHistoryRepository historyRepository,
                           UserServiceClient userServiceClient, PetServiceClient petServiceClient,
                           ShelterServiceClient shelterServiceClient,
                           FollowUpServiceClient followUpServiceClient,
                           UserNotificationClient userNotificationClient, PetNotificationClient petNotificationClient) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.userServiceClient = userServiceClient;
        this.petServiceClient = petServiceClient;
        this.shelterServiceClient = shelterServiceClient;
        this.followUpServiceClient = followUpServiceClient;
        this.userNotificationClient = userNotificationClient;
        this.petNotificationClient = petNotificationClient;
    }

    public List<AdoptionResult> getAdoptions() {
        return this.repository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<AdoptionResult> getAdoptions(String status) {
        try {
            AdoptionStatus adoptionStatus = AdoptionStatus.valueOf(status.toUpperCase());
            return this.repository.findByStatus(adoptionStatus).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para adopción: '{}'", status);
            return List.of();
        }
    }

    @Transactional
    public AdoptionResult create(AdoptionCommand command) {
        log.info("Creando adopción: userId={}, petId={}", command.userId(), command.petId());
        try {
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

            PetResponse pet = petResponse.getBody();
            if (pet != null && pet.shelterId() != null) {
                ResponseEntity<ShelterResponse> shelterResponse = shelterServiceClient.getShelterById(pet.shelterId());
                if (!shelterResponse.getStatusCode().is2xxSuccessful()) {
                    log.warn("Refugio no encontrado: ID={}", pet.shelterId());
                    throw new IllegalArgumentException("El refugio con ID " + pet.shelterId() + " no existe");
                }
            }

            Adoption adoption = new Adoption();
            adoption.setUserId(command.userId());
            adoption.setPetId(command.petId());
            adoption.setStatus(command.status());

            Adoption saved = this.repository.save(adoption);
            recordHistory(saved, "CREATED",
                    "Adopción creada: mascota " + command.petId() + " por usuario " + command.userId());

            String email = userResponse.getBody().email();
            sendUserNotification(command.userId(), "Se ha creado la adopción de la mascota " + command.petId(), "ADOPTION_CREATED", email);
            sendPetNotification(command.userId(), "La mascota " + command.petId() + " ha sido adoptada por el usuario " + command.userId(), "PET_CREATED", command.petId());

            if (pet != null) {
                sendFollowUp(pet.name(), command.userId(), command.petId(), saved.getId());
            }

            log.info("Adopción creada exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear adopción: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al crear adopción: no se pudo completar la validación");
        }
    }

    public Optional<AdoptionResult> getById(Long id) {
        return this.repository.findById(id)
                .map(this::toResult);
    }

    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando adopción: ID={}", id);

        Optional<Adoption> found = this.repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Adopción a eliminar no encontrada: ID={}", id);
            return false;
        }

        Adoption adoption = found.get();

        String email = null;
        try {
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(adoption.getUserId());
            if (userResponse.getStatusCode().is2xxSuccessful()) {
                email = userResponse.getBody().email();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener email del usuario {} para notificación", adoption.getUserId());
        }

        recordHistory(adoption, "DELETED",
                "Adopción eliminada: mascota " + adoption.getPetId() + ", usuario " + adoption.getUserId());

        if (email != null) {
            sendUserNotification(adoption.getUserId(), "La adopción " + id + " ha sido eliminada", "ADOPTION_DELETED", email);
        }
        sendPetNotification(adoption.getUserId(), "La adopción " + id + " de la mascota " + adoption.getPetId() + " ha sido eliminada", "PET_DELETED", adoption.getPetId());

        try {
            this.repository.deleteById(id);
            log.info("Adopción eliminada exitosamente: ID={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar adopción: ID={}", id, e);
            throw e;
        }
    }

    @Transactional
    public Optional<AdoptionResult> updateById(Long id, AdoptionCommand command) {
        log.info("Actualizando adopción: ID={}", id);
        try {
            Optional<Adoption> found = this.repository.findById(id);
            if (found.isEmpty()) {
                log.warn("Adopción no encontrada: ID={}", id);
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

            Adoption adoption = found.get();
            adoption.setUserId(command.userId());
            adoption.setPetId(command.petId());
            adoption.setStatus(command.status());

            Adoption updated = this.repository.save(adoption);
            recordHistory(updated, "UPDATED",
                    "Adopción modificada: mascota " + command.petId() + ", usuario " + command.userId()
                            + ", estado " + command.status());

            String email = userResponse.getBody().email();
            sendUserNotification(command.userId(), "La adopción " + id + " ha sido actualizada a estado " + command.status(), "ADOPTION_UPDATED", email);
            sendPetNotification(command.userId(), "La adopción " + id + " de la mascota " + command.petId() + " ha sido actualizada a estado " + command.status(), "PET_UPDATED", command.petId());

            log.info("Adopción actualizada exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar adopción: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al actualizar adopción: no se pudo completar la validación");
        }
    }

    public List<AdoptionHistoryResponse> getHistory(Long adoptionId) {
        return historyRepository.findByAdoptionIdOrderByCreatedAtDesc(adoptionId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void recordHistory(Adoption adoption, String action, String description) {
        AdoptionHistory history = new AdoptionHistory();
        history.setAdoption(adoption);
        history.setAction(action);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void sendUserNotification(Long userId, String message, String typeName, String email) {
        try {
            UserNotificationRequest request = new UserNotificationRequest(userId, email, message, typeName, "SENT");
            userNotificationClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a {}: {}", email, e.getMessage());
        }
    }

    private void sendFollowUp(String petName, Long userId, Long petId, Long adoptionId) {
        try {
            FollowUpRequest request = new FollowUpRequest(
                    "Usuario " + userId,
                    petName,
                    userId,
                    petId,
                    adoptionId,
                    LocalDateTime.now().plusDays(30),
                    "Seguimiento post-adopción para mascota " + petId,
                    "PENDING"
            );
            followUpServiceClient.createFollowUp(request);
            log.info("Seguimiento creado para adopción: mascota={}, userId={}, adoptionId={}", petName, userId, adoptionId);
        } catch (Exception e) {
            log.warn("No se pudo crear seguimiento en followup-service: {}", e.getMessage());
        }
    }

    private void sendPetNotification(Long userId, String message, String typeName, Long petId) {
        try {
            PetNotificationRequest request = new PetNotificationRequest(userId, petId.toString(), message, typeName, "SENT");
            petNotificationClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a mascota {}: {}", petId, e.getMessage());
        }
    }

    private AdoptionResult toResult(Adoption adoption) {
        return new AdoptionResult(
                adoption.getId(),
                adoption.getUserId(),
                adoption.getPetId(),
                adoption.getStatus(),
                adoption.getCreatedAt(),
                adoption.getUpdatedAt()
        );
    }

    private AdoptionHistoryResponse toHistoryResponse(AdoptionHistory history) {
        return new AdoptionHistoryResponse(
                history.getId(),
                history.getAdoption().getId(),
                history.getAction(),
                history.getDescription(),
                history.getCreatedAt()
        );
    }
}
