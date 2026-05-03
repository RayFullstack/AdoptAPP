package com.adoptapp.petservice.service;

import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PetService {

        private final PetRepository repository;

        public PetService(PetRepository repository) {
            this.repository = repository;
        }

        public List<PetResult> getPets() {
            return this.repository.findAllByOrderByCreatedAtAsc().stream()
                    .map(this::toResult)
                    .toList();
        }

        public List<PetResult> getPets(String statusFilter) {
            if (statusFilter == null || statusFilter.isBlank()) {
                return getPets();
            }
            return this.repository.findByStatusIgnoreCase(statusFilter).stream()
                    .map(this::toResult)
                    .toList();
        }

        public PetResult create(PetCommand command) {
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

        public Optional<PetResult> getById(Long id) {
            return this.repository.findById(id).map(this::toResult);
        }

        public boolean deleteById(Long id) {
            if (this.repository.existsById(id)) {
                this.repository.deleteById(id);
                return true;
            }
            return false;
        }

        public Optional<PetResult> updateById(Long id, PetCommand command) {
            Optional<Pet> found = this.repository.findById(id);
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
            toUpdate.setHealth(command.health());
            toUpdate.setPersonality(command.personality());
            toUpdate.setFosterId(command.fosterId());

            if (command.status() != null && !command.status().isBlank()) {
                toUpdate.setStatus(command.status());
            }
            Pet saved = this.repository.save(toUpdate);
            return Optional.of(toResult(saved));
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
                    pet.getHealth(),
                    pet.getPersonality(),
                    pet.getFosterId()
            );
        }
    }