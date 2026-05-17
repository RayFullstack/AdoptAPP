package com.adoptapp.petservice.service;

import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetHistoryResult;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetHealth;
import com.adoptapp.petservice.model.PetHistory;
import com.adoptapp.petservice.model.PetStatus;
import com.adoptapp.petservice.repository.HealthRepository;
import com.adoptapp.petservice.repository.PetHistoryRepository;
import com.adoptapp.petservice.repository.PetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PetService {

    private final PetRepository petRepository;
    private final HealthRepository healthRepository;
    private final PetHistoryRepository petHistoryRepository;

    public PetService(PetRepository petRepository,
                      HealthRepository healthRepository,
                      PetHistoryRepository petHistoryRepository) {
        this.petRepository = petRepository;
        this.healthRepository = healthRepository;
        this.petHistoryRepository = petHistoryRepository;
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

    public PetResult create(PetCommand command) {
        log.info("Creando mascota: '{}'", command.name());

        PetStatus petStatus;
        try {
            petStatus = PetStatus.valueOf(command.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para mascota: '{}'", command.status());
            throw e;
        }

        PetHealth petHealth = new PetHealth();
        petHealth.setVaccinated(command.vaccinated());
        petHealth.setSterilized(command.sterilized());
        petHealth.setDiseases(command.diseases());

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
        pet.setHealth(petHealth);

        try {
            Pet saved = this.petRepository.save(pet);
            recordChange(saved.getId(), null, null, saved.getName(),
                    null, saved.getStatus().name(),
                    null, saved.getFosterId(), "Mascota creada");
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

    public boolean deleteById(Long id) {
        log.info("Eliminando mascota: ID={}", id);

        try {
            if (this.petRepository.existsById(id)) {
                this.petRepository.deleteById(id);
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

        PetHealth updateHealth = new PetHealth();
        updateHealth.setVaccinated(command.vaccinated());
        updateHealth.setSterilized(command.sterilized());
        updateHealth.setDiseases(command.diseases());

        toUpdate.setHealth(updateHealth);

        if (command.status() != null && !command.status().isBlank()) {
            PetStatus petStatus = PetStatus.valueOf(command.status().toUpperCase());
            toUpdate.setStatus(petStatus);
        }

        try {
            Pet saved = this.petRepository.save(toUpdate);
            recordChange(id, null, previousName, saved.getName(),
                    previousStatus, saved.getStatus().name(),
                    previousFosterId, saved.getFosterId(), null);
            log.info("Mascota actualizada exitosamente: ID={}", id);
            return Optional.of(toResult(saved));
        } catch (Exception e) {
            log.error("Error al actualizar mascota: ID={}", id, e);
            throw e;
        }
    }

    private PetResult toResult(Pet pet) {
        PetHealth health = pet.getHealth();
        return new PetResult(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getRace(),
                pet.getAge(),
                pet.getSize(),
                pet.getColor(),
                pet.getStatus().name(),
                health != null ? health.getVaccinated() : null,
                health != null ? health.getSterilized() : null,
                health != null ? health.getDiseases() : null,
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
}
