package com.adoptapp.adoptionservice.service;

import com.adoptapp.adoptionservice.client.PetNotificationClient;
import com.adoptapp.adoptionservice.client.PetServiceClient;
import com.adoptapp.adoptionservice.client.UserNotificationClient;
import com.adoptapp.adoptionservice.client.UserServiceClient;
import com.adoptapp.adoptionservice.dto.AdoptionCommand;
import com.adoptapp.adoptionservice.dto.AdoptionHistoryResponse;
import com.adoptapp.adoptionservice.dto.AdoptionResult;
import com.adoptapp.adoptionservice.dto.PetNotificationRequest;
import com.adoptapp.adoptionservice.dto.UserNotificationRequest;
import com.adoptapp.adoptionservice.dto.UserResponse;
import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.model.AdoptionHistory;
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
    private final UserNotificationClient userNotificationClient;
    private final PetNotificationClient petNotificationClient;

    public AdoptionService(AdoptionRepository repository, AdoptionHistoryRepository historyRepository,
                           UserServiceClient userServiceClient, PetServiceClient petServiceClient,
                           UserNotificationClient userNotificationClient, PetNotificationClient petNotificationClient) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.userServiceClient = userServiceClient;
        this.petServiceClient = petServiceClient;
        this.userNotificationClient = userNotificationClient;
        this.petNotificationClient = petNotificationClient;
    }

    public List<AdoptionResult> getAdoptions() {
        return this.repository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<AdoptionResult> getAdoptions(String status) {
        return this.repository.findByStatusIgnoreCase(status).stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional
    public AdoptionResult create(AdoptionCommand command) {
        log.info("Creando adopción: userId={}, petId={}", command.userId(), command.petId());

        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Usuario no encontrado: ID={}", command.userId());
            throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
        }

        ResponseEntity<Void> petResponse = petServiceClient.getPetById(command.petId());
        if (!petResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Mascota no encontrada: ID={}", command.petId());
            throw new IllegalArgumentException("La mascota con ID " + command.petId() + " no existe");
        }

        Adoption adoption = new Adoption();
        adoption.setUserId(command.userId());
        adoption.setPetId(command.petId());
        adoption.setStatus(command.status());
        adoption.setCreatedAt(LocalDateTime.now());

        try {
            Adoption saved = this.repository.save(adoption);
            recordHistory(saved.getId(), "CREATED",
                    "Adopción creada: mascota " + command.petId() + " por usuario " + command.userId());

            String email = userResponse.getBody().email();
            sendUserNotification("Adopción creada", "Se ha creado la adopción de la mascota " + command.petId(), "INFO", email);
            sendPetNotification("Adopción creada", "La mascota " + command.petId() + " ha sido adoptada por el usuario " + command.userId(), "INFO", command.petId());

            log.info("Adopción creada exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear adopción", e);
            throw e;
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

        recordHistory(id, "DELETED",
                "Adopción eliminada: mascota " + adoption.getPetId() + ", usuario " + adoption.getUserId());

        if (email != null) {
            sendUserNotification("Adopción eliminada", "La adopción " + id + " ha sido eliminada", "WARN", email);
        }
        sendPetNotification("Adopción eliminada", "La adopción " + id + " de la mascota " + adoption.getPetId() + " ha sido eliminada", "WARN", adoption.getPetId());

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

        ResponseEntity<Void> petResponse = petServiceClient.getPetById(command.petId());
        if (!petResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Mascota no encontrada: ID={}", command.petId());
            throw new IllegalArgumentException("La mascota con ID " + command.petId() + " no existe");
        }

        Adoption adoption = found.get();
        adoption.setUserId(command.userId());
        adoption.setPetId(command.petId());
        adoption.setStatus(command.status());

        try {
            Adoption updated = this.repository.save(adoption);
            recordHistory(id, "UPDATED",
                    "Adopción modificada: mascota " + command.petId() + ", usuario " + command.userId()
                            + ", estado " + command.status());

            String email = userResponse.getBody().email();
            sendUserNotification("Adopción actualizada", "La adopción " + id + " ha sido actualizada a estado " + command.status(), "INFO", email);
            sendPetNotification("Adopción actualizada", "La adopción " + id + " de la mascota " + command.petId() + " ha sido actualizada a estado " + command.status(), "INFO", command.petId());

            log.info("Adopción actualizada exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (Exception e) {
            log.error("Error al actualizar adopción: ID={}", id, e);
            throw e;
        }
    }

    public List<AdoptionHistoryResponse> getHistory(Long adoptionId) {
        return historyRepository.findByAdoptionIdOrderByCreatedAtDesc(adoptionId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void recordHistory(Long adoptionId, String action, String description) {
        AdoptionHistory history = new AdoptionHistory();
        history.setAdoptionId(adoptionId);
        history.setAction(action);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void sendUserNotification(String title, String message, String type, String email) {
        try {
            UserNotificationRequest request = new UserNotificationRequest(title, message, type, email);
            userNotificationClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a {}: {}", email, e.getMessage());
        }
    }

    private void sendPetNotification(String title, String message, String type, Long petId) {
        try {
            PetNotificationRequest request = new PetNotificationRequest(title, message, type, petId.toString());
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
                adoption.getCreatedAt()
        );
    }

    private AdoptionHistoryResponse toHistoryResponse(AdoptionHistory history) {
        return new AdoptionHistoryResponse(
                history.getId(),
                history.getAdoptionId(),
                history.getAction(),
                history.getDescription(),
                history.getCreatedAt()
        );
    }
}
