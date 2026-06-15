package com.adoptapp.userservice.service;

import com.adoptapp.userservice.client.UserNotificationClient;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.userservice.dto.UserCommand;
import com.adoptapp.userservice.dto.UserHistoryResult;
import com.adoptapp.userservice.dto.UserNotificationRequest;
import com.adoptapp.userservice.dto.UserResult;
import com.adoptapp.userservice.model.*;
import com.adoptapp.userservice.repository.AddressRepository;
import com.adoptapp.userservice.repository.PhoneRepository;
import com.adoptapp.userservice.repository.UserHistoryRepository;
import com.adoptapp.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final AddressRepository addressRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserNotificationClient notificationClient;

    public UserService(UserRepository userRepository,
                       PhoneRepository phoneRepository,
                       AddressRepository addressRepository,
                       UserHistoryRepository userHistoryRepository,
                       PasswordEncoder passwordEncoder,
                       UserNotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.phoneRepository = phoneRepository;
        this.addressRepository = addressRepository;
        this.userHistoryRepository = userHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationClient = notificationClient;
    }

    public List<UserResult> getUsers() {
        return this.userRepository.findByStatusNotOrderByCreatedAtAsc(UserStatus.INACTIVE).stream()
                .map(this::toResult)
                .toList();
    }

    public List<UserResult> getUsers(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return getUsers();
        }
        try {
            return this.userRepository
                    .findByStatus(com.adoptapp.userservice.model.UserStatus.valueOf(statusFilter.toUpperCase()))
                    .stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para usuario: '{}'", statusFilter);
            throw new IllegalArgumentException("Status invalido: " + statusFilter);
        }
    }

    @Transactional
    public UserResult create(UserCommand command) {
        log.info("Creando usuario: '{}'", command.username());

        boolean existsByUsername = this.userRepository.existsByUsernameIgnoreCase(command.username());
        boolean existsByEmail = this.userRepository.existsByEmailIgnoreCase(command.email());

        if (existsByUsername) {
            log.warn("Nombre de usuario duplicado: '{}'", command.username());
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está en uso: \"" + command.username() + "\"");
        }
        if (existsByEmail) {
            log.warn("Email duplicado: '{}'", command.email());
            throw new IllegalArgumentException(
                    "El email ya está en uso: \"" + command.email() + "\"");
        }

        User user = new User();

        user.setUsername(command.username());
        user.setName(command.name());
        user.setSurname(command.surname());
        user.setEmail(command.email());
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setStatus(command.status() != null ? command.status() : UserStatus.ACTIVE);
        validateRoleAssignment(command.role());
        user.setRole(command.role() != null ? command.role() : User.Role.ADOPTER);
        user.setActive(command.active());

        UserPhone userPhone = new UserPhone();
        userPhone.setNumber(command.phone());

        userPhone.setUser(user);
        user.setPhone(userPhone);

        UserAddress userAddress = new UserAddress();
        userAddress.setCity(command.city());
        userAddress.setCountry(command.country());
        userAddress.setStreet(command.street());
        userAddress.setHomeNumber(command.homeNumber());
        userAddress.setType(command.type());
        userAddress.setPostalCode(command.postalCode());

        userAddress.setPrimaryAddress(true);

        userAddress.setUser(user);

        user.setAddresses(List.of(userAddress));

        try {
            User saved = this.userRepository.save(user);
            sendNotification(saved.getId(), saved.getEmail(), "Usuario creado: " + saved.getName(), "USER_CREATED");
            log.info("Usuario creado exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear usuario", e);
            throw e;
        }
    }

    public Optional<UserResult> getById(Long id) {
        return this.userRepository.findById(id)
                .filter(user -> user.getStatus() != UserStatus.INACTIVE)
                .map(this::toResult);
    }

    public Optional<UserResult> getByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .filter(user -> user.getStatus() != UserStatus.INACTIVE)
                .map(this::toResult);
    }

    public Optional<UserAuthResponse> getAuthByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .map(user -> new UserAuthResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRole().name(),
                        user.isActive()
                ));
    }

    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando usuario: ID={}", id);

        try {
            Optional<User> found = this.userRepository.findById(id);
            if (found.isPresent()) {
                User user = found.get();
                if (user.getStatus() == UserStatus.INACTIVE) {
                    log.warn("Usuario ya inactivo: ID={}", id);
                    return false;
                }

                user.setStatus(UserStatus.INACTIVE);
                user.setActive(false);
                this.userRepository.save(user);
                sendNotification(id, user.getEmail(), "Usuario eliminado: " + user.getName(), "USER_DELETED");
                log.info("Usuario eliminado exitosamente: ID={}", id);
                return true;
            }
            log.warn("Usuario a eliminar no encontrado: ID={}", id);
            return false;
        } catch (Exception e) {
            log.error("Error al eliminar usuario: ID={}", id, e);
            throw e;
        }
    }

    public Optional<List<UserHistoryResult>> getHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            return Optional.empty();
        }
        List<UserHistoryResult> historial = userHistoryRepository
                .findByUserIdOrderByChangedAtDesc(userId)
                .stream()
                .map(this::toHistoryResult)
                .toList();
        return Optional.of(historial);
    }

    @Transactional
    public Optional<UserResult> updateById(Long id, UserCommand command) {
        log.info("Actualizando usuario: ID={}", id);

        Optional<User> found = this.userRepository.findById(id);

        if (found.isEmpty()) {
            log.warn("Usuario no encontrado: ID={}", id);
            return Optional.empty();
        }

        boolean existsByUsername = this.userRepository.existsByUsernameIgnoreCaseAndIdNot(command.username(), id);
        boolean existsByEmail = this.userRepository.existsByEmailIgnoreCaseAndIdNot(command.email(), id);

        if (existsByUsername) {
            log.warn("Nombre de usuario duplicado: '{}', ID={}", command.username(), id);
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está en uso: \"" + command.username() + "\"");
        }
        if (existsByEmail) {
            log.warn("Email duplicado: '{}', ID={}", command.email(), id);
            throw new IllegalArgumentException(
                    "El email ya está en uso: \"" + command.email() + "\"");
        }

        User toUpdate = found.get();
        if (toUpdate.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalArgumentException("No se puede actualizar un usuario inactivo");
        }

        String oldName = toUpdate.getName();
        String oldSurname = toUpdate.getSurname();
        String oldUsername = toUpdate.getUsername();
        String oldEmail = toUpdate.getEmail();
        String oldPhone = toUpdate.getPhone() != null ? toUpdate.getPhone().getNumber() : null;
        String oldStatus = toUpdate.getStatus() != null ? toUpdate.getStatus().name() : null;
        String oldRole = toUpdate.getRole() != null ? toUpdate.getRole().name() : null;
        boolean oldActive = toUpdate.isActive();

        // DATOS BÁSICOS
        toUpdate.setUsername(command.username());
        toUpdate.setName(command.name());
        toUpdate.setSurname(command.surname());
        toUpdate.setEmail(command.email());

        // STATUS
        if (command.status() != null) {
            toUpdate.setStatus(command.status());
        }

        // ROLE
        if (command.role() != null && command.role() != toUpdate.getRole()) {
            throw new IllegalArgumentException("No se permite cambiar el rol desde esta operacion");
        }

        // ACTIVE
        toUpdate.setActive(command.active());

        // PHONE
        UserPhone phone = toUpdate.getPhone();

        if (phone == null) {
            phone = new UserPhone();
            phone.setUser(toUpdate);
        }

        phone.setNumber(command.phone());
        toUpdate.setPhone(phone);

        // ADDRESS
        UserAddress address;

        if (toUpdate.getAddresses() == null || toUpdate.getAddresses().isEmpty()) {

            address = new UserAddress();
            address.setUser(toUpdate);

            toUpdate.setAddresses(new ArrayList<>(List.of(address)));

        } else {

            address = toUpdate.getAddresses().get(0);
        }

        address.setCity(command.city());
        address.setCountry(command.country());
        address.setStreet(command.street());
        address.setHomeNumber(command.homeNumber());
        address.setPostalCode(command.postalCode());
        address.setType(command.type());
        address.setPrimaryAddress(true);

        User saved;
        try {
            saved = this.userRepository.save(toUpdate);
        } catch (Exception e) {
            log.error("Error al actualizar usuario: ID={}", id, e);
            throw e;
        }

        log.info("Usuario actualizado exitosamente: ID={}", id);

        sendNotification(saved.getId(), saved.getEmail(), "Usuario actualizado: " + saved.getName(), "USER_UPDATED");

        recordChange(
                saved.getId(),
                oldName, command.name(),
                oldSurname, command.surname(),
                oldUsername, command.username(),
                oldEmail, command.email(),
                oldPhone, command.phone(),
                oldStatus, command.status() != null ? command.status().name() : null,
                oldRole, command.role() != null ? command.role().name() : null,
                oldActive, command.active(),
                LocalDateTime.now(),
                "Usuario actualizado"
        );

        return Optional.of(toResult(saved));
    }

    private UserResult toResult(User user) {

        UserAddress address = null;

        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            address = user.getAddresses().get(0);
        }

        return new UserResult(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),

                user.getPhone() != null
                        ? user.getPhone().getNumber()
                        : null,

                address != null ? address.getCountry() : null,
                address != null ? address.getCity() : null,
                address != null ? address.getStreet() : null,
                address != null ? address.getHomeNumber() : null,
                address != null ? address.getPostalCode() : null,
                address != null ? address.getType() : null,

                user.getStatus(),

                user.getRole(),

                user.isActive()
        );
    }

    private UserHistoryResult toHistoryResult(UserHistory h) {
        return new UserHistoryResult(
                h.getId(),
                h.getUser() != null ? h.getUser().getId() : null,
                h.getPreviousName(),
                h.getNewName(),
                h.getPreviousSurname(),
                h.getNewSurname(),
                h.getPreviousUsername(),
                h.getNewUsername(),
                h.getPreviousEmail(),
                h.getNewEmail(),
                h.getPreviousPhone(),
                h.getNewPhone(),
                h.getPreviousStatus(),
                h.getNewStatus(),
                h.getPreviousRole(),
                h.getNewRole(),
                h.getPreviousActive(),
                h.getNewActive(),
                h.getChangedAt(),
                h.getComment()
        );
    }

    private void sendNotification(Long userId, String email, String message, String typeName) {
        try {
            UserNotificationRequest request = new UserNotificationRequest(userId, email, message, typeName, "SENT");
            notificationClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a {}: {}", email, e.getMessage());
        }
    }

    private void recordChange(Long userId,
                              String previousName,
                              String newName,
                              String previousSurname,
                              String newSurname,
                              String previousUsername,
                              String newUsername,
                              String previousEmail,
                              String newEmail,
                              String previousPhone,
                              String newPhone,
                              String previousStatus,
                              String newStatus,
                              String previousRole,
                              String newRole,
                              boolean previousActive,
                              boolean newActive,
                              LocalDateTime changedAt,
                              String comment) {
        boolean statusChanged = newStatus != null
                && !newStatus.equalsIgnoreCase(previousStatus == null ? "" : previousStatus);
        boolean emailChanged = !java.util.Objects.equals(previousEmail, newEmail);
        boolean nameChanged = !java.util.Objects.equals(previousName, newName);
        boolean surnameChanged = !java.util.Objects.equals(previousSurname, newSurname);
        boolean usernameChanged = !java.util.Objects.equals(previousUsername, newUsername);
        boolean phoneChanged = !java.util.Objects.equals(previousPhone, newPhone);
        boolean roleChanged = !java.util.Objects.equals(previousRole, newRole);
        boolean activeChanged = previousActive != newActive;

        if (!statusChanged && !emailChanged && !nameChanged && !surnameChanged && !usernameChanged && !phoneChanged && !roleChanged && !activeChanged) return;


        UserHistory entry = new UserHistory();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        entry.setUser(user);
        entry.setPreviousName(nameChanged ? previousName : null);
        entry.setNewName(nameChanged ? newName : null);
        entry.setPreviousSurname(surnameChanged ? previousSurname : null);
        entry.setNewSurname(surnameChanged ? newSurname : null);
        entry.setPreviousUsername(usernameChanged ? previousUsername : null);
        entry.setNewUsername(usernameChanged ? newUsername : null);
        entry.setPreviousPhone(phoneChanged ? previousPhone : null);
        entry.setNewPhone(phoneChanged ? newPhone : null);
        entry.setPreviousStatus(statusChanged ? previousStatus : null);
        entry.setNewStatus(statusChanged ? newStatus : null);
        entry.setPreviousEmail(emailChanged ? previousEmail : null);
        entry.setNewEmail(emailChanged ? newEmail : null);
        entry.setPreviousRole(roleChanged ? previousRole : null);
        entry.setNewRole(roleChanged ? newRole : null);
        entry.setPreviousActive(activeChanged ? previousActive : null);
        entry.setNewActive(activeChanged ? newActive : null);
        entry.setChangedAt(LocalDateTime.now());
        entry.setComment(comment);
        userHistoryRepository.save(entry);
    }

    private void validateRoleAssignment(User.Role role) {
        if (role == User.Role.ADMIN) {
            throw new IllegalArgumentException(
                    "No se permite asignar usuarios con rol ADMIN desde esta operación");
        }
    }

    @Transactional
    public UserResult register(UserCommand command) {
        UserCommand adopterCommand = new UserCommand(
                command.username(),
                command.name(),
                command.surname(),
                command.email(),
                command.password(),
                command.phone(),
                command.country(),
                command.city(),
                command.street(),
                command.homeNumber(),
                command.postalCode(),
                command.type(),
                UserStatus.ACTIVE,
                User.Role.ADOPTER,
                true
        );

        return create(adopterCommand);
    }
}

