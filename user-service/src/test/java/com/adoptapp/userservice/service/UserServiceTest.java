package com.adoptapp.userservice.service;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.userservice.client.UserNotificationClient;
import com.adoptapp.userservice.dto.UserCommand;
import com.adoptapp.userservice.dto.UserHistoryResponse;
import com.adoptapp.userservice.dto.UserResult;
import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.model.UserHistory;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.repository.AddressRepository;
import com.adoptapp.userservice.repository.PhoneRepository;
import com.adoptapp.userservice.repository.UserHistoryRepository;
import com.adoptapp.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PhoneRepository phoneRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private UserHistoryRepository userHistoryRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserNotificationClient notificationClient;

    @InjectMocks
    private UserService service;

    @Test
    void create_shouldCreateActiveAdopter_whenDataIsValid() {
        UserCommand command = command("adopter.demo", "adopter@mail.com", User.Role.ADOPTER, UserStatus.ACTIVE, true);

        when(userRepository.existsByUsernameIgnoreCase("adopter.demo")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("adopter@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResult result = service.create(command);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("adopter.demo");
        assertThat(result.email()).isEqualTo("adopter@mail.com");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.role()).isEqualTo(User.Role.ADOPTER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_shouldThrow_whenUsernameAlreadyExists() {
        UserCommand command = command("adopter.demo", "adopter@mail.com", User.Role.ADOPTER, UserStatus.ACTIVE, true);
        when(userRepository.existsByUsernameIgnoreCase("adopter.demo")).thenReturn(true);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de usuario");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrow_whenEmailAlreadyExists() {
        UserCommand command = command("adopter.demo", "adopter@mail.com", User.Role.ADOPTER, UserStatus.ACTIVE, true);
        when(userRepository.existsByUsernameIgnoreCase("adopter.demo")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("adopter@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrow_whenRoleIsAdmin() {
        UserCommand command = command("admin.demo", "admin@mail.com", User.Role.ADMIN, UserStatus.ACTIVE, true);
        when(userRepository.existsByUsernameIgnoreCase("admin.demo")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("admin@mail.com")).thenReturn(false);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se permite asignar usuarios con rol ADMIN");
    }

    @Test
    void register_shouldForceAdopterActiveRole() {
        UserCommand command = command("shelter.admin", "admin@mail.com", User.Role.SHELTER_ADMIN, UserStatus.SUSPENDED, false);
        when(userRepository.existsByUsernameIgnoreCase("shelter.admin")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("admin@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        UserResult result = service.register(command);

        assertThat(result.role()).isEqualTo(User.Role.ADOPTER);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.active()).isTrue();
    }

    @Test
    void getUsers_shouldHideInactiveUsers() {
        when(userRepository.findByStatusNotOrderByCreatedAtAsc(UserStatus.INACTIVE))
                .thenReturn(List.of(user(1L, UserStatus.ACTIVE, true)));

        List<UserResult> result = service.getUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void getUsers_shouldFilterByStatus_whenStatusIsValid() {
        when(userRepository.findByStatus(UserStatus.SUSPENDED)).thenReturn(List.of(user(1L, UserStatus.SUSPENDED, false)));

        List<UserResult> result = service.getUsers("SUSPENDED");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void getUsers_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.getUsers("wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status invalido: wrong");
    }

    @Test
    void getByEmail_shouldReturnUser_whenUserIsActive() {
        when(userRepository.findByEmail("adopter@mail.com")).thenReturn(Optional.of(user(1L, UserStatus.ACTIVE, true)));

        Optional<UserResult> result = service.getByEmail("adopter@mail.com");

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("adopter@mail.com");
    }

    @Test
    void getByEmail_shouldReturnEmpty_whenUserIsInactive() {
        when(userRepository.findByEmail("adopter@mail.com")).thenReturn(Optional.of(user(1L, UserStatus.INACTIVE, false)));

        assertThat(service.getByEmail("adopter@mail.com")).isEmpty();
    }

    @Test
    void getAuthByEmail_shouldReturnAuthData_evenWhenUserIsInactive() {
        when(userRepository.findByEmail("adopter@mail.com")).thenReturn(Optional.of(user(1L, UserStatus.INACTIVE, false)));

        Optional<UserAuthResponse> result = service.getAuthByEmail("adopter@mail.com");

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().role()).isEqualTo("ADOPTER");
        assertThat(result.get().enabled()).isFalse();
    }

    @Test
    void deleteById_shouldMarkUserInactive_whenUserExists() {
        User user = user(1L, UserStatus.ACTIVE, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.deleteById(1L);

        assertThat(result).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void deleteById_shouldReturnFalse_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.deleteById(99L)).isFalse();
    }

    @Test
    void deleteById_shouldReturnFalse_whenUserAlreadyInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserStatus.INACTIVE, false)));

        assertThat(service.deleteById(1L)).isFalse();
    }

    @Test
    void getById_shouldReturnEmpty_whenUserIsInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserStatus.INACTIVE, false)));

        assertThat(service.getById(1L)).isEmpty();
    }

    @Test
    void getHistory_shouldReturnEmpty_whenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThat(service.getHistory(1L)).isEmpty();
    }

    @Test
    void getHistory_shouldReturnHistory_whenUserExists() {
        UserHistory history = new UserHistory();
        history.setId(3L);
        history.setUser(user(1L, UserStatus.ACTIVE, true));
        history.setPreviousName("Camila");
        history.setNewName("Cami");
        history.setChangedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        history.setComment("Usuario actualizado");
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userHistoryRepository.findByUserIdOrderByChangedAtDesc(1L)).thenReturn(List.of(history));

        Optional<List<UserHistoryResponse>> result = service.getHistory(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().getFirst().newName()).isEqualTo("Cami");
    }

    @Test
    void updateById_shouldUpdateUser_whenDataIsValid() {
        User user = user(1L, UserStatus.ACTIVE, true);
        UserCommand command = command("adopter.updated", "updated@mail.com", User.Role.ADOPTER, UserStatus.SUSPENDED, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("adopter.updated", 1L)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("updated@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserResult> result = service.updateById(1L, command);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("adopter.updated");
        assertThat(result.get().email()).isEqualTo("updated@mail.com");
        assertThat(result.get().status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(result.get().active()).isFalse();
        verify(userHistoryRepository).save(any(UserHistory.class));
    }

    @Test
    void updateById_shouldReturnEmpty_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(service.updateById(1L, command("adopter.demo", "adopter@mail.com", User.Role.ADOPTER, UserStatus.ACTIVE, true))).isEmpty();
    }

    @Test
    void updateById_shouldThrow_whenUserIsInactive() {
        User user = user(1L, UserStatus.INACTIVE, false);
        UserCommand command = command("adopter.demo", "adopter@mail.com", User.Role.ADOPTER, UserStatus.ACTIVE, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("adopter.demo", 1L)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("adopter@mail.com", 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateById(1L, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede actualizar un usuario inactivo");
    }

    @Test
    void updateById_shouldThrow_whenUsernameAlreadyExists() {
        User user = user(1L, UserStatus.ACTIVE, true);
        UserCommand command = command("duplicado", "adopter@mail.com", User.Role.ADOPTER, UserStatus.ACTIVE, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("duplicado", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateById(1L, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de usuario");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateById_shouldThrow_whenEmailAlreadyExists() {
        User user = user(1L, UserStatus.ACTIVE, true);
        UserCommand command = command("adopter.demo", "duplicado@mail.com", User.Role.ADOPTER, UserStatus.ACTIVE, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("adopter.demo", 1L)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("duplicado@mail.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateById(1L, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateById_shouldThrow_whenChangingRoleToAdmin() {
        User user = user(1L, UserStatus.ACTIVE, true);
        UserCommand command = command("adopter.demo", "adopter@mail.com", User.Role.ADMIN, UserStatus.ACTIVE, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("adopter.demo", 1L)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("adopter@mail.com", 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateById(1L, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se permite cambiar el rol desde esta operacion");

        verify(userRepository, never()).save(any(User.class));
    }

    private UserCommand command(String username, String email, User.Role role, UserStatus status, boolean active) {
        return new UserCommand(username, "Camila", "Rios", email, "secret123", "123456789",
                "Chile", "Santiago", "Calle 1", "123", "8320000", "HOME", status, role, active);
    }

    private User user(Long id, UserStatus status, boolean active) {
        User user = new User();
        user.setId(id);
        user.setUsername("adopter.demo");
        user.setName("Camila");
        user.setSurname("Rios");
        user.setEmail("adopter@mail.com");
        user.setPassword("encoded");
        user.setStatus(status);
        user.setRole(User.Role.ADOPTER);
        user.setActive(active);
        return user;
    }
}