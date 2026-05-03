package com.adoptapp.userservice.service;

import com.adoptapp.userservice.dto.UserCommand;
import com.adoptapp.userservice.dto.UserResult;
import com.adoptapp.userservice.repository.UserRepository;
import com.adoptapp.userservice.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<UserResult> getUsers() {
        return this.repository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toResult)
                .toList();
    }

    public List<UserResult> getUsers(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return getUsers();
        }
        return this.repository.findByStatusIgnoreCase(statusFilter).stream()
                .map(this::toResult)
                .toList();
    }

    public UserResult create(UserCommand command) {
        boolean exists = this.repository.existsByUsernameIgnoreCase(command.username());
        if (exists) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está en uso: \"" + command.username() + "\"");
        }

        User user = new User();
        user.setUsername(command.username());
        user.setName(command.name());
        user.setSurname(command.surname());
        user.setEmail(command.email());
        user.setAddress(command.address());
        user.setPhone(command.phone());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        User saved = this.repository.save(user);
        return toResult(saved);
    }

    public Optional<UserResult> getById(Long id) {
        return this.repository.findById(id).map(this::toResult);
    }

    public boolean deleteById(Long id) {
        if (this.repository.existsById(id)) {
            this.repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<UserResult> updateById(Long id, UserCommand command) {
        Optional<User> found = this.repository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        User toUpdate = found.get();

        toUpdate.setUsername(command.username());
        toUpdate.setEmail(command.email());
        toUpdate.setName(command.name());
        toUpdate.setSurname(command.surname());
        toUpdate.setAddress(command.address());
        toUpdate.setPhone(command.phone());
        if (command.status() != null && !command.status().isBlank()) {
            toUpdate.setStatus(command.status());
        }
        User saved = this.repository.save(toUpdate);
        return Optional.of(toResult(saved));
    }

    private UserResult toResult(User user) {
        return new UserResult(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

