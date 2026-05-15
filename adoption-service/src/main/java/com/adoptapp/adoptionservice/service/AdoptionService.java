package com.adoptapp.adoptionservice.service;

import com.adoptapp.adoptionservice.dto.AdoptionCommand;
import com.adoptapp.adoptionservice.dto.AdoptionResult;
import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.repository.AdoptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdoptionService {

    private final AdoptionRepository repository;

    public AdoptionService(AdoptionRepository repository) {
        this.repository = repository;
    }

    public List<AdoptionResult> getAdoptions() {
        return this.repository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<AdoptionResult> getAdoptions(String status) {
        return this.repository.findAll().stream()
                .filter(adoption ->
                        adoption.getStatus() != null &&
                                adoption.getStatus().equalsIgnoreCase(status))
                .map(this::toResult)
                .toList();
    }

    public AdoptionResult create(AdoptionCommand command) {
        Adoption adoption = new Adoption();

        adoption.setPetName(command.petName());
        adoption.setAdopterName(command.adopterName());
        adoption.setStatus(command.status());
        adoption.setPetId(command.petId());
        adoption.setUserId(command.userId());
        adoption.setCreatedAt(LocalDateTime.now());

        Adoption saved = this.repository.save(adoption);

        return toResult(saved);
    }

    public Optional<AdoptionResult> getById(Long id) {
        return this.repository.findById(id)
                .map(this::toResult);
    }

    public boolean deleteById(Long id) {
        if (!this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    public Optional<AdoptionResult> updateById(Long id, AdoptionCommand command) {
        Optional<Adoption> found = this.repository.findById(id);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        Adoption adoption = found.get();

        adoption.setPetName(command.petName());
        adoption.setAdopterName(command.adopterName());
        adoption.setStatus(command.status());
        adoption.setPetId(command.petId());
        adoption.setUserId(command.userId());

        Adoption updated = this.repository.save(adoption);

        return Optional.of(toResult(updated));
    }

    private AdoptionResult toResult(Adoption adoption) {
        return new AdoptionResult(
                adoption.getId(),
                adoption.getPetName(),
                adoption.getAdopterName(),
                adoption.getStatus()
        );
    }
}