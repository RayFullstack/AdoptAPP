package com.adoptapp.adoptionservice.service;

import com.adoptapp.adoptionservice.client.FollowUpServiceClient;
import com.adoptapp.adoptionservice.client.PetNotificationClient;
import com.adoptapp.adoptionservice.client.PetServiceClient;
import com.adoptapp.adoptionservice.client.ShelterServiceClient;
import com.adoptapp.adoptionservice.client.StaffServiceClient;
import com.adoptapp.adoptionservice.client.UserNotificationClient;
import com.adoptapp.adoptionservice.client.UserServiceClient;
import com.adoptapp.adoptionservice.dto.*;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
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

    private static final List<AdoptionStatus> VISIBLE_STATUSES = List.of(
            AdoptionStatus.PENDING,
            AdoptionStatus.APPROVED,
            AdoptionStatus.REJECTED
    );

    private static final List<AdoptionStatus> ACTIVE_STATUSES = List.of(
            AdoptionStatus.PENDING,
            AdoptionStatus.APPROVED
    );

    private final AdoptionRepository repository;
    private final AdoptionHistoryRepository historyRepository;
    private final UserServiceClient userServiceClient;
    private final PetServiceClient petServiceClient;
    private final ShelterServiceClient shelterServiceClient;
    private final StaffServiceClient staffServiceClient;
    private final FollowUpServiceClient followUpServiceClient;
    private final UserNotificationClient userNotificationClient;
    private final PetNotificationClient petNotificationClient;

    public AdoptionService(AdoptionRepository repository, AdoptionHistoryRepository historyRepository,
                           UserServiceClient userServiceClient, PetServiceClient petServiceClient,
                           ShelterServiceClient shelterServiceClient, StaffServiceClient staffServiceClient,
                           FollowUpServiceClient followUpServiceClient,
                           UserNotificationClient userNotificationClient, PetNotificationClient petNotificationClient) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.userServiceClient = userServiceClient;
        this.petServiceClient = petServiceClient;
        this.shelterServiceClient = shelterServiceClient;
        this.staffServiceClient = staffServiceClient;
        this.followUpServiceClient = followUpServiceClient;
        this.userNotificationClient = userNotificationClient;
        this.petNotificationClient = petNotificationClient;
    }

    public List<AdoptionResult> getAdoptions() {
        return this.repository.findByStatusIn(VISIBLE_STATUSES).stream()
                .map(this::toResult)
                .toList();
    }

    public List<AdoptionResult> getAdoptionsIncludingCancelledByStatus(String status) {
        try {
            AdoptionStatus adoptionStatus = AdoptionStatus.valueOf(status.toUpperCase());
            return this.repository.findByStatus(adoptionStatus).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado invalido para adopcion: '{}'", status);
            throw new IllegalArgumentException("Status invalido: " + status);
        }
    }

    public List<AdoptionResult> getAdoptionsByShelter(Long shelterId) {
        return getAdoptions().stream()
                .filter(adoption -> belongsToShelter(adoption.petId(), shelterId))
                .toList();
    }

    public List<AdoptionResult> getAdoptionsByShelter(Long shelterId, String status) {
        return getAdoptionsIncludingCancelledByStatus(status).stream()
                .filter(adoption -> belongsToShelter(adoption.petId(), shelterId))
                .toList();
    }

    public List<AdoptionResult> getAdoptionsByUser(Long userId) {
        return this.repository.findByUserIdAndStatusIn(userId, VISIBLE_STATUSES).stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<AdoptionResult> getById(Long id) {
        return this.repository.findById(id)
                .filter(adoption -> adoption.getStatus() != AdoptionStatus.CANCELLED)
                .map(this::toResult);
    }


    public Optional<AdoptionResult> getByIdIncludingCancelled(Long id) {

        return this.repository.findById(id).map(this::toResult);
    }

    public Optional<AdoptionResult> getByIdIncludingCancelledForShelter(Long id, Long shelterId) {
        return this.repository.findById(id)
                .filter(adoption -> belongsToShelter(adoption.getPetId(), shelterId))
                .map(this::toResult);
    }

    public Optional<AdoptionResult> getByIdForShelter(Long id, Long shelterId) {
        return this.repository.findById(id)
                .filter(adoption -> adoption.getStatus() != AdoptionStatus.CANCELLED)
                .filter(adoption -> belongsToShelter(adoption.getPetId(), shelterId))
                .map(this::toResult);
    }

    public Optional<AdoptionResult> getByIdForUser(Long id, Long userId) {
        return this.repository.findById(id)
                .filter(adoption -> adoption.getStatus() != AdoptionStatus.CANCELLED)
                .filter(adoption -> userId.equals(adoption.getUserId()))
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


    @Transactional
    public AdoptionResult create(AdoptionCommand command) {
        return create(command, null);
    }

    @Transactional
    public AdoptionResult createForShelterAdmin(AdoptionCommand command, Long shelterId) {
        return create(command, shelterId);
    }

    private AdoptionResult create(AdoptionCommand command, Long allowedShelterId) {
        log.info("Creando adopcion: userId={}, petId={}", command.userId(), command.petId());
        try {
            UserResponse user = getExistingUser(command.userId());
            PetResponse pet = getExistingPet(command.petId());
            validatePetAvailable(command.petId(), pet);
            validatePetShelter(command.petId(), pet);
            validatePetBelongsToShelter(command.petId(), pet, allowedShelterId);
            validateNoActiveAdoption(command.petId());

            Adoption adoption = new Adoption();
            adoption.setUserId(command.userId());
            adoption.setPetId(command.petId());
            adoption.setStatus(AdoptionStatus.PENDING);

            Adoption saved = this.repository.save(adoption);
            recordHistory(saved, "CREATED",
                    "Adopcion creada: mascota " + command.petId() + " por usuario " + command.userId());

            sendUserNotification(command.userId(), "Se ha creado la solicitud de adopcion de la mascota " + command.petId(), "ADOPTION_CREATED", user.email());

            log.info("Adopcion creada exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear adopcion: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al crear adopcion: no se pudo completar la validacion");
        }
    }


    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando adopcion: ID={}", id);

        Optional<Adoption> found = this.repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Adopcion a eliminar no encontrada: ID={}", id);
            return false;
        }

        Adoption adoption = found.get();

        if (adoption.getStatus() == AdoptionStatus.CANCELLED) {
            log.warn("Adopcion ya cancelada: ID={}", id);
            return false;
        }

        boolean shouldMakePetAvailable = false;

        if (adoption.getStatus() == AdoptionStatus.APPROVED) {
            boolean hasOtherApprovedAdoption = repository.existsByPetIdAndStatusInAndIdNot(
                    adoption.getPetId(),
                    List.of(AdoptionStatus.APPROVED),
                    id
            );

            shouldMakePetAvailable = !hasOtherApprovedAdoption;
        }

        String email = null;
        try {
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(adoption.getUserId());
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                email = userResponse.getBody().email();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener email del usuario {} para notificacion", adoption.getUserId());
        }

        if (shouldMakePetAvailable) {
            syncPetStatusOrFail(adoption.getPetId(), "AVAILABLE", id);
        }

        adoption.setStatus(AdoptionStatus.CANCELLED);
        Adoption updated = this.repository.save(adoption);

        recordHistory(updated, "CANCELLED",
                "Adopcion eliminada: mascota " + adoption.getPetId() + ", usuario " + adoption.getUserId());

        if (email != null) {
            try {
                sendUserNotification(adoption.getUserId(), "La adopcion " + id + " ha sido eliminada", "ADOPTION_DELETED", email);
            } catch (Exception e) {
                log.warn("No se pudo enviar notificacion de eliminacion al usuario {}", adoption.getUserId());
            }
        }
        try {
            sendPetNotification(adoption.getUserId(), "La adopcion " + id + " de la mascota " + adoption.getPetId() + " ha sido eliminada", "PET_DELETED", adoption.getPetId());
        } catch (Exception e) {
            log.warn("No se pudo enviar notificacion de eliminacion para mascota {}", adoption.getPetId());
        }

        log.info("Adopcion eliminada exitosamente: ID={}", id);
        return true;
    }

    @Transactional
    public Optional<AdoptionResult> updateById(Long id, AdoptionCommand command) {
        log.info("Actualizando adopcion: ID={}", id);

        try {
            Optional<Adoption> found = this.repository.findById(id);
            if (found.isEmpty()) {
                log.warn("Adopcion no encontrada: ID={}", id);
                return Optional.empty();
            }

            Adoption adoption = found.get();

            if (adoption.getStatus() == AdoptionStatus.CANCELLED) {
                throw new IllegalArgumentException("NO SE PUEDE ACTUALIZAR UNA ADOPCION CANCELADA");
            }

            if (command.status() == null) {
                throw new IllegalArgumentException("El estado de la adopcion es requerido para actualizar");
            }

            UserResponse user = getExistingUser(adoption.getUserId());
            PetResponse pet = getExistingPet(adoption.getPetId());

            if (command.status() == AdoptionStatus.APPROVED) {
                boolean hasOtherActiveAdoption = repository.existsByPetIdAndStatusInAndIdNot(
                        adoption.getPetId(),
                        List.of(AdoptionStatus.PENDING, AdoptionStatus.APPROVED),
                        id
                );

                if (hasOtherActiveAdoption) {
                    throw new IllegalArgumentException(
                            "La mascota ya tiene otra adopcion pendiente o aprobada"
                    );
                }
            }
            AdoptionStatus previousStatus = adoption.getStatus();
            validateStatusTransition(previousStatus, command.status());

            if (previousStatus != AdoptionStatus.APPROVED
                    && command.status() == AdoptionStatus.APPROVED
                    && !"AVAILABLE".equalsIgnoreCase(pet.status())) {
                throw new IllegalArgumentException("La mascota con ID " + adoption.getPetId() + " no esta disponible para adopcion");
            }

            boolean shouldMakePetNotAvailable = previousStatus != AdoptionStatus.APPROVED
                    && command.status() == AdoptionStatus.APPROVED;

            if (shouldMakePetNotAvailable) {
                syncPetStatusOrFail(adoption.getPetId(), "NOT_AVAILABLE", id);
            }

            adoption.setStatus(command.status());

            Adoption updated = this.repository.save(adoption);

            recordHistory(updated, "UPDATED",
                    "Adopcion modificada: mascota " + adoption.getPetId() + ", usuario " + adoption.getUserId()
                            + ", estado " + command.status());

            if (previousStatus != AdoptionStatus.APPROVED
                    && command.status() == AdoptionStatus.APPROVED) {
                sendFollowUp(pet.name(), adoption.getUserId(), adoption.getPetId(), updated.getId());
            }

            sendUserNotification(adoption.getUserId(), "La adopcion " + id + " ha sido actualizada a estado " + command.status(), "ADOPTION_UPDATED", user.email());
            sendPetNotification(adoption.getUserId(), "La adopcion " + id + " de la mascota " + adoption.getPetId() + " ha sido actualizada a estado " + command.status(), "PET_UPDATED", adoption.getPetId());

            log.info("Adopcion actualizada exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar adopcion: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al actualizar adopcion: no se pudo completar la validacion");
        }

    }

    public List<AdoptionHistoryResponse> getHistory(Long adoptionId) {
        if (!repository.existsById(adoptionId)) {
            throw new IllegalArgumentException("La adopcion con ID " + adoptionId + " no existe");
        }

        return historyRepository.findByAdoptionIdOrderByCreatedAtDesc(adoptionId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private UserResponse getExistingUser(Long userId) {
        ResponseEntity<UserResponse> response = userServiceClient.getUserById(userId);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("Usuario no encontrado: ID={}", userId);
            throw new IllegalArgumentException("El usuario con ID " + userId + " no existe");
        }
        return response.getBody();
    }

    private PetResponse getExistingPet(Long petId) {
        ResponseEntity<PetResponse> response = petServiceClient.getPetById(petId);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("Mascota no encontrada: ID={}", petId);
            throw new IllegalArgumentException("La mascota con ID " + petId + " no existe");
        }
        return response.getBody();
    }

    private void validatePetAvailable(Long petId, PetResponse pet) {
        if (!"AVAILABLE".equalsIgnoreCase(pet.status())) {
            throw new IllegalArgumentException("La mascota con ID " + petId + " no esta disponible para adopcion");
        }
    }

    private void validatePetShelter(Long petId, PetResponse pet) {
        if (pet.shelterId() == null) {
            throw new IllegalArgumentException("La mascota con ID " + petId + " no tiene refugio asociado");
        }

        ResponseEntity<ShelterResponse> response = shelterServiceClient.getShelterById(pet.shelterId());
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("Refugio no encontrado: ID={}", pet.shelterId());
            throw new IllegalArgumentException("El refugio con ID " + pet.shelterId() + " no existe");
        }
    }

    private void validatePetBelongsToShelter(Long petId, PetResponse pet, Long allowedShelterId) {
        if (allowedShelterId == null) {
            return;
        }

        if (!allowedShelterId.equals(pet.shelterId())) {
            throw new IllegalArgumentException(
                    "La mascota con ID " + petId + " no pertenece al refugio del usuario autenticado"
            );
        }
    }

    private void validateNoActiveAdoption(Long petId) {
        boolean hasActiveAdoption = repository.existsByPetIdAndStatusIn(petId, ACTIVE_STATUSES);
        if (hasActiveAdoption) {
            throw new IllegalArgumentException("La mascota ya tiene una adopcion pendiente o aprobada");
        }
    }

    private void validateStatusTransition(AdoptionStatus previousStatus, AdoptionStatus newStatus) {
        if (previousStatus == newStatus) {
            return;
        }

        boolean validTransition =
                previousStatus == AdoptionStatus.PENDING
                        && (newStatus == AdoptionStatus.APPROVED
                        || newStatus == AdoptionStatus.REJECTED);

        if (!validTransition) {
            throw new IllegalArgumentException("Transicion de estado no permitida: " + previousStatus + " -> " + newStatus);
        }
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
                    "Seguimiento post-adopcion para mascota " + petId,
                    "PENDING"
            );
            followUpServiceClient.createFollowUp(request);
            log.info("Seguimiento creado para adopcion: mascota={}, userId={}, adoptionId={}", petName, userId, adoptionId);
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

    private void syncPetStatusOrFail(Long petId, String status, Long adoptionId) {
        try {
            ResponseEntity<PetResponse> response = petServiceClient.updatePetStatus(
                    petId,
                    new PetStatusRequest(status)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Pet-service no pudo actualizar mascota " + petId + " a estado " + status);
            }
        } catch (Exception e) {
            log.error("Fallo sincronizando mascota {} a estado {} para adopcion {}: {}",
                    petId, status, adoptionId, e.getMessage());
            throw new RuntimeException("No se pudo sincronizar el estado de la mascota " + petId);
        }
    }

    private boolean belongsToShelter(Long petId, Long shelterId) {
        try {
            ResponseEntity<PetResponse> response = petServiceClient.getPetById(petId);
            return response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && shelterId.equals(response.getBody().shelterId());
        } catch (Exception e) {
            log.warn("No se pudo validar refugio de mascota {}: {}", petId, e.getMessage());
            return false;
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
