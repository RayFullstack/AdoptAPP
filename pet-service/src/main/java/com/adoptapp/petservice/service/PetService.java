package com.adoptapp.petservice.service;

import com.adoptapp.petservice.client.HealthServiceClient;
import com.adoptapp.petservice.client.NotificationServiceClient;
import com.adoptapp.petservice.client.ShelterServiceClient;
import com.adoptapp.petservice.client.UserServiceClient;
import com.adoptapp.petservice.dto.HealthRequest;
import com.adoptapp.petservice.dto.NotificationRequest;
import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetHistoryResult;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetHistory;
import com.adoptapp.petservice.model.PetStatus;
import com.adoptapp.petservice.model.SterilizationStatus;
import com.adoptapp.petservice.model.VaccinationStatus;
import com.adoptapp.petservice.repository.PetHistoryRepository;
import com.adoptapp.petservice.repository.PetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PetService(PetRepository petRepository,
                      PetHistoryRepository petHistoryRepository,
                      NotificationServiceClient notificationClient,
                      UserServiceClient userClient,
                      HealthServiceClient healthClient,
                      ShelterServiceClient shelterClient) {
        this.petRepository = petRepository;
        this.petHistoryRepository = petHistoryRepository;
        this.notificationClient = notificationClient;
        this.userClient = userClient;
        this.healthClient = healthClient;
        this.shelterClient = shelterClient;
    }

    public List<PetResult> getPets() {
        return this.petRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toResult)
                .toList();
    }

    public List<PetResult> getPets(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return getPets();
        }
        try {
            PetStatus petStatus = PetStatus.valueOf(statusFilter.toUpperCase());
            return this.petRepository
                    .findByStatus(petStatus)
                    .stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Transactional
    public PetResult create(PetCommand command) {
        log.info("Creando mascota: '{}'", command.name());

        if (command.shelterId() != null) {
            try {
                var shelterResponse = shelterClient.getShelterById(command.shelterId());
                if (!shelterResponse.getStatusCode().is2xxSuccessful()) {
                    log.warn("Refugio no encontrado: ID={}", command.shelterId());
                    throw new IllegalArgumentException("El refugio con ID " + command.shelterId() + " no existe");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                log.warn("No se pudo validar refugio: shelterId={}", command.shelterId());
            }
        }

        PetStatus petStatus;
        try {
            petStatus = PetStatus.valueOf(command.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para mascota: '{}'", command.status());
            throw e;
        }

        Pet pet = new Pet();
        pet.setName(command.name());
        pet.setSpecies(command.species());
        pet.setRace(command.race());
        pet.setAge(command.age());
        pet.setSize(command.size());
        pet.setColor(command.color());
        pet.setPersonality(command.personality());
        pet.setFosterId(command.fosterId());
        pet.setShelterId(command.shelterId());
        pet.setStatus(petStatus);

        try {
            Pet saved = this.petRepository.save(pet);

            VaccinationStatus vax = Boolean.TRUE.equals(command.vaccinated())
                    ? VaccinationStatus.VACCINATED : VaccinationStatus.NOT_VACCINATED;
            SterilizationStatus ster = Boolean.TRUE.equals(command.sterilized())
                    ? SterilizationStatus.STERILIZED : SterilizationStatus.NOT_STERILIZED;

            try {
                var healthResponse = healthClient.createHealth(new HealthRequest(
                        saved.getFosterId(), saved.getId(), vax, ster, command.diseases()
                ));
                if (healthResponse.getBody() != null) {
                    saved.setHealthServiceId(healthResponse.getBody().id());
                    this.petRepository.save(saved);
                }
            } catch (Exception e) {
                log.warn("No se pudo crear registro de salud en health-service: {}", e.getMessage());
            }

            recordChange(saved.getId(), null, null, saved.getName(),
                    null, saved.getStatus().name(),
                    null, saved.getFosterId(), "Mascota creada");
            sendNotification(saved.getFosterId(), saved.getName(), "PET_CREATED",
                    "La mascota " + saved.getName() + " ha sido registrada");
            log.info("Mascota creada exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear mascota", e);
            throw e;
        }
    }

    public Optional<PetResult> getById(Long id) {
        return this.petRepository.findById(id).map(this::toResult);
    }

    public Optional<HealthResult> getHealthInfo(Long petId) {
        return this.petRepository.findById(petId)
                .map(Pet::getHealthServiceId)
                .filter(healthId -> healthId != null)
                .flatMap(healthId -> {
                    try {
                        ResponseEntity<HealthResult> response = healthClient.getHealth(healthId);
                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                            return Optional.of(response.getBody());
                        }
                        log.warn("Health-service no retornó datos para healthServiceId={}", healthId);
                        return Optional.empty();
                    } catch (Exception e) {
                        log.error("Error obteniendo salud desde health-service para petId={}: {}", petId, e.getMessage());
                        return Optional.empty();
                    }
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando mascota: ID={}", id);

        try {
            if (this.petRepository.existsById(id)) {
                Pet pet = this.petRepository.findById(id).orElse(null);
                if (pet != null && pet.getHealthServiceId() != null) {
                    try {
                        healthClient.deleteHealth(pet.getHealthServiceId());
                    } catch (Exception e) {
                        log.warn("No se pudo eliminar salud en health-service: {}", e.getMessage());
                    }
                }
                String name = pet != null ? pet.getName() : "Desconocida";
                this.petRepository.deleteById(id);
                sendNotification(null, name, "PET_DELETED",
                        "La mascota " + name + " ha sido eliminada");
                log.info("Mascota eliminada exitosamente: ID={}", id);
                return true;
            }
            log.warn("Mascota a eliminar no encontrada: ID={}", id);
            return false;
        } catch (Exception e) {
            log.error("Error al eliminar mascota: ID={}", id, e);
            throw e;
        }
    }

    @Transactional
    public Optional<PetResult> updateById(Long id, PetCommand command) {
        log.info("Actualizando mascota: ID={}", id);

        Optional<Pet> found = this.petRepository.findById(id);
        if (found.isEmpty()) {
            log.warn("Mascota no encontrada: ID={}", id);
            return Optional.empty();
        }

        Pet toUpdate = found.get();
        String previousName = toUpdate.getName();
        String previousStatus = toUpdate.getStatus().name();
        Long previousFosterId = toUpdate.getFosterId();

        toUpdate.setName(command.name());
        toUpdate.setSpecies(command.species());
        toUpdate.setRace(command.race());
        toUpdate.setAge(command.age());
        toUpdate.setSize(command.size());
        toUpdate.setColor(command.color());
        toUpdate.setPersonality(command.personality());
        toUpdate.setFosterId(command.fosterId());

        if (command.status() != null && !command.status().isBlank()) {
            PetStatus petStatus = PetStatus.valueOf(command.status().toUpperCase());
            toUpdate.setStatus(petStatus);
        }

        try {
            Pet saved = this.petRepository.save(toUpdate);

            if (saved.getHealthServiceId() != null) {
                try {
                    VaccinationStatus vax = Boolean.TRUE.equals(command.vaccinated())
                            ? VaccinationStatus.VACCINATED : VaccinationStatus.NOT_VACCINATED;
                    SterilizationStatus ster = Boolean.TRUE.equals(command.sterilized())
                            ? SterilizationStatus.STERILIZED : SterilizationStatus.NOT_STERILIZED;
                    healthClient.updateHealth(saved.getHealthServiceId(), new HealthRequest(
                            saved.getFosterId(), saved.getId(), vax, ster, command.diseases()
                    ));
                } catch (Exception e) {
                    log.warn("No se pudo actualizar salud en health-service: {}", e.getMessage());
                }
            }

            recordChange(id, null, previousName, saved.getName(),
                    previousStatus, saved.getStatus().name(),
                    previousFosterId, saved.getFosterId(), null);
            sendNotification(saved.getFosterId(), saved.getName(), "PET_UPDATED",
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
                pet.getFosterId(),
                pet.getShelterId()
        );
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
                h.getPreviousFosterId(),
                h.getNewFosterId(),
                h.getChangedByUserId(),
                h.getChangedAt(),
                h.getComment()
        );
    }

    private void recordChange(Long petId, Long userId, String previousName, String newName,
                              String previousStatus, String newStatus,
                              Long previousFosterId, Long newFosterId, String comment) {
        boolean changed = false;

        if (!java.util.Objects.equals(previousName, newName)) changed = true;
        if (!java.util.Objects.equals(previousStatus, newStatus)) changed = true;
        if (!java.util.Objects.equals(previousFosterId, newFosterId)) changed = true;

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
        entry.setPreviousFosterId(!java.util.Objects.equals(previousFosterId, newFosterId) ? previousFosterId : null);
        entry.setNewFosterId(!java.util.Objects.equals(previousFosterId, newFosterId) ? newFosterId : null);
        entry.setComment(comment);
        petHistoryRepository.save(entry);
    }

    private void sendNotification(Long fosterId, String petName, String typeName, String message) {
        try {
            String recipient = "sistema@adoptapp.com";
            if (fosterId != null) {
                var userResponse = userClient.getUserById(fosterId);
                if (userResponse.getBody() != null) {
                    recipient = userResponse.getBody().email();
                }
            }
            NotificationRequest request = new NotificationRequest(
                    fosterId,
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
