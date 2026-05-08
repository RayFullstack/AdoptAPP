package com.adoptapp.adoptionservice.service;

import com.adoptapp.adoptionservice.dto.AdoptionCommand;
import com.adoptapp.adoptionservice.dto.AdoptionResult;
import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.repository.AdoptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdoptionService {
    private final AdoptionRepository repository;

    public AdoptionService(AdoptionRepository repository) {
        this.repository = repository;
    }

    public List<AdoptionResult> getAdoptions() {
        return this.repository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toResult)
                .toList();
    }

    public List<AdoptionResult> getAdoptions(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return getAdoptions();
        }
        return this.repository.findByStatusIgnoreCase(statusFilter).stream()
                .map(this::toResult)
                .toList();
    }

    public AdoptionResult create(AdoptionCommand command) {
        boolean exists = this.repository.existsByNameIgnoreCase(command.name());
        if (exists) {
            throw new IllegalArgumentException(
                    "El nombre ya está en uso: \"" + command.name() + "\"");
        }

        Pet pet = new Pet();
        pet.setName(command.name());
        pet.setSpecies(command.species());
        pet.setRace(command.race());
        pet.setAge(command.age());
        pet.setSize(command.size());
        pet.setColor(command.color());
        pet.setHealth(command.health());
        pet.setPersonality(command.personality());
        pet.setFosterId(command.fosterId());
        pet.setStatus(command.status());
        Pet saved = this.repository.save(pet);
        return toResult(saved);
    }

    public Optional<AdoptionResult> getById(Long id) {
        return this.repository.findById(id).map(this::toResult);
    }

    public boolean deleteById(Long id) {
        if (this.repository.existsById(id)) {
            this.repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<AdoptionResult> updateById(Long id, AdoptionCommand command) {
        Optional<Adoption> found = this.repository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        Adoption toUpdate = found.get();

        toUpdate.setName(command.name());
        toUpdate.setSpecies(command.species());
        toUpdate.setRace(command.race());
        toUpdate.setAge(command.age());
        toUpdate.setSize(command.size());
        toUpdate.setColor(command.color());
        toUpdate.setHealth(command.health());
        toUpdate.setPersonality(command.personality());
        toUpdate.setFosterId(command.fosterId());

        if (command.status() != null && !command.status().isBlank()) {
            toUpdate.setStatus(command.status());
        }
        Adoption saved = this.repository.save(toUpdate);
        return Optional.of(toResult(saved));
    }

    private AdoptionResult toResult(Adoption adoption) {
        return new AdoptionResult(
                adoption.getId(),
                adoption.getName(),
                adoption.getSpecies(),
                adoption.getRace(),
                adoption.getAge(),
                adoption.getSize(),
                adoption.getColor(),
                adoption.getHealth(),
                adoption.getPersonality(),
                adoption.getFosterId()
        );
    }
}

