package com.adoptapp.petservice.service;

import com.adoptapp.petservice.client.HealthServiceClient;
import com.adoptapp.petservice.client.NotificationServiceClient;
import com.adoptapp.petservice.client.ShelterServiceClient;
import com.adoptapp.petservice.client.StaffServiceClient;
import com.adoptapp.petservice.client.UserServiceClient;
import com.adoptapp.petservice.dto.HealthResult;
import com.adoptapp.petservice.dto.NotificationRequest;
import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetHistoryResult;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetHistory;
import com.adoptapp.petservice.model.PetStatus;
import com.adoptapp.petservice.repository.PetHistoryRepository;
import com.adoptapp.petservice.repository.PetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PetService {

    private final PetRepository petRepository;
    private final PetHistoryRepository petHistoryRepository;
    private final NotificationServiceClient notificationClient;
    private final UserServiceClient userClient;
    private final HealthServiceClient healthClient;
    private final ShelterServiceClient shelterClient;
    private final StaffServiceClient staffClient;

    public PetService(PetRepository petRepository,
                      PetHistoryRepository petHistoryRepository,
                      NotificationServiceClient notificationClient,
                      UserServiceClient userClient,
                      HealthServiceClient healthClient,
                      ShelterServiceClient shelterClient,
                      StaffServiceClient staffClient) {
        this.petRepository = petRepository;
        this.petHistoryRepository = petHistoryRepository;
        this.notificationClient = notificationClient;
        this.userClient = userClient;
        this.healthClient = healthClient;
        this.shelterClient = shelterClient;
        this.staffClient = staffClient;
    }


    public List<PetResult> getPets() {
        return this.petRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(PetStatus.AVAILABLE, PetStatus.NOT_AVAILABLE)).stream()
                .map(this::toResult)
                .toList();
    }


    public List<PetResult> getPets(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return getPets();
        }
        try {
            PetStatus petStatus = parsePetStatus(statusFilter);
            return this.petRepository
                    .findByStatus(petStatus)
                    .stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status invalido: " + statusFilter);
        }
    }

    public List<PetResult> getPetsByShelter(Long shelterId) {
        return this.petRepository.findByShelterIdAndStatusInOrderByCreatedAtAsc(
                shelterId,
                List.of(PetStatus.AVAILABLE, PetStatus.NOT_AVAILABLE)).stream()
                .map(this::toResult)
                .toList();
    }

    public List<PetResult> getPetsByShelter(Long shelterId, String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return getPetsByShelter(shelterId);
        }
        try {
            PetStatus petStatus = parsePetStatus(statusFilter);
            return this.petRepository
                    .findByShelterIdAndStatus(shelterId, petStatus)
                    .stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status invalido: " + statusFilter);
        }
    }


    public Optional<PetResult> getById(Long id) {

        return this.petRepository.findById(id)
                .filter(pet -> pet.getStatus() != PetStatus.DELETED)
                .map(this::toResult);
    }


    public Optional<PetResult> getByIdIncludingDeleted(Long id) {

        return this.petRepository.findById(id).map(this::toResult);
    }

    public Optional<PetResult> getByIdIncludingDeletedForShelter(Long id, Long shelterId) {
        return this.petRepository.findById(id)
                .filter(pet -> shelterId.equals(pet.getShelterId()))
                .map(this::toResult);
    }

    public Optional<PetResult> getByIdForShelter(Long id, Long shelterId) {
        return this.petRepository.findById(id)
                .filter(pet -> pet.getStatus() != PetStatus.DELETED)
                .filter(pet -> shelterId.equals(pet.getShelterId()))
                .map(this::toResult);
    }

    public Long getUserIdByEmail(String email) {
        ResponseEntity<UserAuthResponse> response = userClient.getUserAuthByEmail(email);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("Usuario autenticado no encontrado: " + email);
        }
        return response.getBody().id();
    }

    public Long getShelterIdForStaffUser(Long userId) {
        var response = staffClient.getStaffByUserId(userId);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("El usuario no tiene staff activo asociado");
        }
        return response.getBody().shelterId();
    }


    @Transactional
    public PetResult create(PetCommand command) {
        return create(command, null);
    }

    @Transactional
    public PetResult createForShelter(PetCommand command, Long shelterId) {
        return create(command, shelterId);
    }

    private PetResult create(PetCommand command, Long allowedShelterId) {
        log.info("Creando mascota: '{}'", command.name());

        validateShelterExists(command.shelterId());

        validateShelterOwnership(command.shelterId(), allowedShelterId);

        PetStatus petStatus = parseEditablePetStatus(command.status());

        Pet pet = new Pet();
        pet.setName(command.name());
        pet.setSpecies(command.species());
        pet.setRace(command.race());
        pet.setAge(command.age());
        pet.setSize(command.size());
        pet.setColor(command.color());
        pet.setPersonality(command.personality());
        pet.setShelterId(command.shelterId());
        pet.setStatus(petStatus);

        try {
            Pet saved = this.petRepository.save(pet);
            recordChange(saved.getId(), null, null, saved.getName(),
                    null, saved.getStatus().name(),
                    "Mascota creada");
            sendNotification(saved.getShelterId(), saved.getName(), "PET_CREATED",
                    "La mascota " + saved.getName() + " ha sido registrada");
            log.info("Mascota creada exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear mascota", e);
            throw e;
        }
    }


    public Optional<HealthResult> getHealthInfo(Long petId) {
        Optional<Pet> found = this.petRepository.findById(petId);
        if (found.isEmpty() || found.get().getStatus() == PetStatus.DELETED) {
            return Optional.empty();
        }

        try {
            ResponseEntity<HealthResult> response = healthClient.getHealthByPetId(petId);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }

            log.warn("Health-service no retornó datos para petId={}", petId);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error obteniendo salud desde health-service para petId={}: {}", petId, e.getMessage());
            return Optional.empty();
        }
    }


    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando mascota: ID={}", id);

        try {
            Optional<Pet> found = this.petRepository.findById(id);
            if (found.isEmpty()) {
                log.warn("Mascota a eliminar no encontrada: ID={}", id);
                return false;
            }

            Pet pet = found.get();
            if (pet.getStatus() == PetStatus.DELETED) {
                log.warn("Mascota ya eliminada: ID={}", id);
                return false;
            }

            String name = pet.getName();
            String previousStatus = pet.getStatus().name();

            deleteHealthOrFail(id);

            pet.setStatus(PetStatus.DELETED);
            this.petRepository.save(pet);
            recordChange(id, null, name, name,
                    previousStatus, PetStatus.DELETED.name(),
                    "Mascota marcada como eliminada");

            sendNotification(pet.getShelterId(), name, "PET_DELETED",
                    "La mascota " + name + " ha sido eliminada");
            log.info("Mascota eliminada exitosamente: ID={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar mascota: ID={}", id, e);
            throw e;
        }
    }

    private void deleteHealthOrFail(Long petId) {
        try {
            ResponseEntity<Void> healthResponse = healthClient.deleteHealthByPetId(petId);
            if (!healthResponse.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Health-service no pudo eliminar salud asociada a petId=" + petId);
            }
        } catch (Exception e) {
            log.error("No se pudo eliminar salud asociada al petId={}: {}", petId, e.getMessage());
            throw new RuntimeException("No se pudo eliminar la ficha de salud asociada a la mascota " + petId);
        }
    }


    @Transactional
    public Optional<PetResult> updateById(Long id, PetCommand command) {
        return updateById(id, command, null);
    }

    @Transactional
    public Optional<PetResult> updateByIdForShelter(Long id, PetCommand command, Long shelterId) {
        return updateById(id, command, shelterId);
    }

    private Optional<PetResult> updateById(Long id, PetCommand command, Long allowedShelterId) {
        log.info("Actualizando mascota: ID={}", id);

        Optional<Pet> found = this.petRepository.findById(id);
        if (found.isEmpty()) {
            log.warn("Mascota no encontrada: ID={}", id);
            return Optional.empty();
        }

        validateShelterExists(command.shelterId());
        validateShelterOwnership(command.shelterId(), allowedShelterId);

        Pet toUpdate = found.get();

        if (toUpdate.getStatus() == PetStatus.DELETED) {
            throw new IllegalArgumentException("No se puede actualizar una mascota eliminada");
        }

        validateShelterOwnership(toUpdate.getShelterId(), allowedShelterId);

        String previousName = toUpdate.getName();
        String previousStatus = toUpdate.getStatus().name();
        String previousSpecies = toUpdate.getSpecies();
        String previousRace = toUpdate.getRace();
        Integer previousAge = toUpdate.getAge();
        String previousSize = toUpdate.getSize();
        String previousColor = toUpdate.getColor();
        String previousPersonality = toUpdate.getPersonality();
        Long previousShelterId = toUpdate.getShelterId();

        toUpdate.setName(command.name());
        toUpdate.setSpecies(command.species());
        toUpdate.setRace(command.race());
        toUpdate.setAge(command.age());
        toUpdate.setSize(command.size());
        toUpdate.setColor(command.color());
        toUpdate.setPersonality(command.personality());
        toUpdate.setShelterId(command.shelterId());

        if (command.status() != null && !command.status().isBlank()) {
            toUpdate.setStatus(parseEditablePetStatus(command.status()));
        }

        try {
            Pet saved = this.petRepository.save(toUpdate);

            recordChange(id, null, previousName, saved.getName(),
                    previousStatus, saved.getStatus().name(),
                    buildUpdateComment(previousSpecies, saved.getSpecies(),
                            previousRace, saved.getRace(),
                            previousAge, saved.getAge(),
                            previousSize, saved.getSize(),
                            previousColor, saved.getColor(),
                            previousPersonality, saved.getPersonality(),
                            previousShelterId, saved.getShelterId()));
            sendNotification(saved.getShelterId(), saved.getName(), "PET_UPDATED",
                    "Los datos de " + saved.getName() + " han sido actualizados");
            log.info("Mascota actualizada exitosamente: ID={}", id);
            return Optional.of(toResult(saved));
        } catch (Exception e) {
            log.error("Error al actualizar mascota: ID={}", id, e);
            throw e;
        }
    }


    private PetResult toResult(Pet pet) {
        return new PetResult(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getRace(),
                pet.getAge(),
                pet.getSize(),
                pet.getColor(),
                pet.getStatus().name(),
                pet.getPersonality(),
                pet.getShelterId()
        );
    }


    public Optional<PetResult> updateByStatus(Long id, String status) {
        log.info("Actualizando estado de mascota: ID={}", id);

        return petRepository.findById(id)
                .map(pet -> {
                    if (pet.getStatus() == PetStatus.DELETED) {
                        throw new IllegalArgumentException("No se puede cambiar el estado de una mascota eliminada");
                    }

                    String previousName = pet.getName();
                    String previousStatus = pet.getStatus() != null ? pet.getStatus().name() : null;
                    PetStatus petStatus = parseEditablePetStatus(status);

                    pet.setStatus(petStatus);

                    try {
                        Pet saved = this.petRepository.save(pet);

                        recordChange(id, null, previousName, saved.getName(),
                                previousStatus, saved.getStatus().name(),
                                "Estado de mascota actualizado");

                        sendNotification(saved.getShelterId(), saved.getName(), "PET_UPDATED",
                                "El estado de " + saved.getName() + " ha sido actualizado");

                        log.info("Estado de mascota actualizado exitosamente: ID={}", id);
                        return toResult(saved);
                    } catch (Exception e) {
                        log.error("Error al actualizar el estado de la mascota: ID={}", id, e);
                        throw e;
                    }
                });
    }


    public Optional<List<PetHistoryResult>> getHistory(Long petId) {
        if (!petRepository.existsById(petId)) {
            return Optional.empty();
        }
        List<PetHistoryResult> history = petHistoryRepository
                .findByPetIdOrderByChangedAtDesc(petId)
                .stream()
                .map(this::toHistoryResult)
                .toList();
        return Optional.of(history);
    }

    private PetHistoryResult toHistoryResult(PetHistory h) {
        return new PetHistoryResult(
                h.getId(),
                h.getPet().getId(),
                h.getPreviousName(),
                h.getNewName(),
                h.getPreviousStatus(),
                h.getNewStatus(),
                h.getChangedByUserId(),
                h.getChangedAt(),
                h.getComment()
        );
    }

    private void recordChange(Long petId, Long userId, String previousName, String newName,
                              String previousStatus, String newStatus, String comment) {
        boolean changed = false;

        if (!java.util.Objects.equals(previousName, newName)) changed = true;
        if (!java.util.Objects.equals(previousStatus, newStatus)) changed = true;
        if (comment != null && !comment.isBlank()) changed = true;

        if (!changed) return;

        PetHistory entry = new PetHistory();
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) return;
        entry.setPet(pet);
        entry.setChangedByUserId(userId);
        entry.setPreviousName(!java.util.Objects.equals(previousName, newName) ? previousName : null);
        entry.setNewName(!java.util.Objects.equals(previousName, newName) ? newName : null);
        entry.setPreviousStatus(!java.util.Objects.equals(previousStatus, newStatus) ? previousStatus : null);
        entry.setNewStatus(!java.util.Objects.equals(previousStatus, newStatus) ? newStatus : null);
        entry.setComment(comment);
        petHistoryRepository.save(entry);
    }

    private void recordComment(Long petId, Long userId, String comment) {
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) return;

        PetHistory entry = new PetHistory();
        entry.setPet(pet);
        entry.setChangedByUserId(userId);
        entry.setComment(comment);
        petHistoryRepository.save(entry);
    }

    private String buildUpdateComment(String previousSpecies, String newSpecies,
                                      String previousRace, String newRace,
                                      Integer previousAge, Integer newAge,
                                      String previousSize, String newSize,
                                      String previousColor, String newColor,
                                      String previousPersonality, String newPersonality,
                                      Long previousShelterId, Long newShelterId) {
        StringBuilder changes = new StringBuilder();
        appendChange(changes, "species", previousSpecies, newSpecies);
        appendChange(changes, "race", previousRace, newRace);
        appendChange(changes, "age", previousAge, newAge);
        appendChange(changes, "size", previousSize, newSize);
        appendChange(changes, "color", previousColor, newColor);
        appendChange(changes, "personality", previousPersonality, newPersonality);
        appendChange(changes, "shelterId", previousShelterId, newShelterId);
        return changes.isEmpty() ? null : "Campos modificados: " + changes;
    }

    private void appendChange(StringBuilder changes, String field, Object previousValue, Object newValue) {
        if (java.util.Objects.equals(previousValue, newValue)) {
            return;
        }

        if (!changes.isEmpty()) {
            changes.append(", ");
        }
        changes.append(field);
    }

    private void validateShelterOwnership(Long requestShelterId, Long allowedShelterId) {
        if (allowedShelterId == null) {
            return;
        }

        if (requestShelterId == null || !allowedShelterId.equals(requestShelterId)) {
            throw new IllegalArgumentException("La mascota no pertenece al refugio del usuario autenticado");
        }
    }

    private void validateShelterExists(Long shelterId) {
        if (shelterId == null) {
            return;
        }

        try {
            var shelterResponse = shelterClient.getShelterById(shelterId);
            if (!shelterResponse.getStatusCode().is2xxSuccessful() || shelterResponse.getBody() == null) {
                log.warn("Refugio no encontrado: ID={}", shelterId);
                throw new IllegalArgumentException("El refugio con ID " + shelterId + " no existe");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("No se pudo validar refugio: shelterId={}", shelterId, e);
            throw new RuntimeException("No se pudo validar el refugio con ID " + shelterId);
        }
    }

    private PetStatus parsePetStatus(String status) {
        return PetStatus.valueOf(status.toUpperCase());
    }

    private PetStatus parseEditablePetStatus(String status) {
        try {
            PetStatus petStatus = parsePetStatus(status);
            validateNotDeletedStatus(petStatus);
            return petStatus;
        } catch (IllegalArgumentException e) {
            log.warn("Estado invalido para mascota: '{}'", status);
            throw new IllegalArgumentException("Estado de mascota invalido: " + status);
        }
    }

    private void validateNotDeletedStatus(PetStatus status) {
        if (status == PetStatus.DELETED) {
            throw new IllegalArgumentException("El estado DELETED solo se puede asignar mediante delete");
        }
    }

    private void sendNotification(Long shelterId, String petName, String typeName, String message) {
        try {
            String recipient = "sistema@adoptapp.com";
            if (shelterId != null) {
                var shelterResponse = shelterClient.getShelterById(shelterId);
                if (shelterResponse.getBody() != null && shelterResponse.getBody().email() != null) {
                    recipient = shelterResponse.getBody().email();
                }
            }
            NotificationRequest request = new NotificationRequest(
                    null,
                    shelterId,
                    recipient,
                    message,
                    typeName,
                    "SENT"
            );
            notificationClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("No se pudo enviar notificacion para '{}': {}", petName, e.getMessage());
        }
    }
}
