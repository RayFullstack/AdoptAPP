package com.adoptapp.petservice.service;

import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetHealth;
import com.adoptapp.petservice.model.PetStatus;
import com.adoptapp.petservice.repository.HealthRepository;
import com.adoptapp.petservice.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final HealthRepository healthRepository;

    public PetService(PetRepository petRepository,
                      HealthRepository healthRepository) {
        this.petRepository = petRepository;
        this.healthRepository = healthRepository;
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
        PetStatus petStatus = PetStatus.valueOf(statusFilter.toUpperCase());
        return this.petRepository
                .findByStatusIgnoreCase(petStatus)
                .stream()
                .map(this::toResult)
                .toList();
    }

    public PetResult create(PetCommand command) {

        PetStatus petStatus = PetStatus.valueOf(command.status().toUpperCase());

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

        pet.setStatus(petStatus);
        pet.setHealth(petHealth);

        Pet saved = this.petRepository.save(pet);
        return toResult(saved);
    }

    public Optional<PetResult> getById(Long id) {
        return this.petRepository.findById(id).map(this::toResult);
    }

    public boolean deleteById(Long id) {
        if (this.petRepository.existsById(id)) {
            this.petRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<PetResult> updateById(Long id, PetCommand command) {
        Optional<Pet> found = this.petRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        Pet toUpdate = found.get();

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
        Pet saved = this.petRepository.save(toUpdate);
        return Optional.of(toResult(saved));
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
                pet.getFosterId()
        );
    }
}