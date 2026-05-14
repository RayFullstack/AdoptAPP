package com.adoptapp.userservice.service;

import com.adoptapp.userservice.dto.UserCommand;
import com.adoptapp.userservice.dto.UserResult;
import com.adoptapp.userservice.model.UserAddress;
import com.adoptapp.userservice.model.UserPhone;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.repository.AddressRepository;
import com.adoptapp.userservice.repository.PhoneRepository;
import com.adoptapp.userservice.repository.UserRepository;
import com.adoptapp.userservice.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final AddressRepository addressRepository;

    public UserService(UserRepository userRepository,
                       PhoneRepository phoneRepository,
                       AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.phoneRepository = phoneRepository;
        this.addressRepository = addressRepository;
    }

    public List<UserResult> getUsers() {
        return this.userRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toResult)
                .toList();
    }

    public List<UserResult> getUsers(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return getUsers();
        }
        return this.userRepository
                .findByStatusIgnoreCase(statusFilter)
                .stream()
                .map(this::toResult)
                .toList();
    }

    public UserResult create(UserCommand command) {
        boolean existsByUsername = this.userRepository.existsByUsernameIgnoreCase(command.username());
        boolean existsByEmail = this.userRepository.existsByEmailIgnoreCase(command.email());

        if (existsByUsername) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está en uso: \"" + command.username() + "\"");
        }
        if (existsByEmail) {
            throw new IllegalArgumentException(
                "El email ya está en uso: \"" + command.email() + "\"");
        }

        User user = new User();

        user.setUsername(command.username());
        user.setName(command.name());
        user.setSurname(command.surname());
        user.setEmail(command.email());
        user.setStatus(UserStatus.ACTIVE);

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
        User saved = this.userRepository.save(user);

        return toResult(saved);
    }

    public Optional<UserResult> getById(Long id) {
        return this.userRepository.findById(id).map(this::toResult);
    }

    public boolean deleteById(Long id) {
        if (this.userRepository.existsById(id)) {
            this.userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<UserResult> updateById(Long id, UserCommand command) {

        Optional<User> found = this.userRepository.findById(id);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        User toUpdate = found.get();

        // DATOS BÁSICOS
        toUpdate.setUsername(command.username());
        toUpdate.setName(command.name());
        toUpdate.setSurname(command.surname());
        toUpdate.setEmail(command.email());

        // STATUS
        if (command.status() != null) {
            toUpdate.setStatus(command.status());
        }

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

            toUpdate.setAddresses(List.of(address));

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

        User saved = this.userRepository.save(toUpdate);

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

                user.getStatus()
        );
    }
}

