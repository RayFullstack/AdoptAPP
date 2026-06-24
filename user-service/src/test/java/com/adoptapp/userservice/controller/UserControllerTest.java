package com.adoptapp.userservice.controller;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.userservice.dto.UserHistoryResponse;
import com.adoptapp.userservice.dto.UserRequest;
import com.adoptapp.userservice.dto.UserResult;
import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.service.UserLinkAssembler;
import com.adoptapp.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService service;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(service, new UserLinkAssembler());
    }

    @Test
    void getAllUsers_shouldReturnOk_whenNoStatusFilter() {
        when(service.getUsers()).thenReturn(List.of(result()));

        ResponseEntity<?> response = controller.getAllUsers(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllUsers_shouldUseStatusFilter_whenStatusIsPresent() {
        when(service.getUsers("ACTIVE")).thenReturn(List.of(result()));

        ResponseEntity<?> response = controller.getAllUsers("ACTIVE");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).getUsers("ACTIVE");
    }

    @Test
    void getUserById_shouldReturnOk_whenUserExists() {
        when(service.getById(1L)).thenReturn(Optional.of(result()));

        ResponseEntity<?> response = controller.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() {
        when(service.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getUserById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserByEmail_shouldReturnOk_whenUserExists() {
        when(service.getByEmail("adopter@mail.com")).thenReturn(Optional.of(result()));

        ResponseEntity<?> response = controller.getUserByEmail("adopter@mail.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserAuthByEmail_shouldReturnOk_whenUserExists() {
        when(service.getAuthByEmail("adopter@mail.com"))
                .thenReturn(Optional.of(new UserAuthResponse(1L, "adopter@mail.com", "encoded", "ADOPTER", true)));

        ResponseEntity<?> response = controller.getUserAuthByEmail("adopter@mail.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getHistory_shouldReturnOk_whenHistoryExists() {
        UserHistoryResponse history = new UserHistoryResponse(1L, 1L, null, "Cami", null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, LocalDateTime.now(), "UPDATED");
        when(service.getHistory(1L)).thenReturn(Optional.of(List.of(history)));

        ResponseEntity<?> response = controller.getHistory(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getHistory_shouldReturnNotFound_whenUserDoesNotExist() {
        when(service.getHistory(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getHistory(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_shouldReturnCreated_whenRequestIsValid() {
        when(service.create(any())).thenReturn(result());

        ResponseEntity<?> response = controller.create(request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void registerUser_shouldReturnCreated_whenRequestIsValid() {
        when(service.register(any())).thenReturn(result());

        ResponseEntity<?> response = controller.registerUser(request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateUserById_shouldReturnOk_whenUserExists() {
        when(service.updateById(any(), any())).thenReturn(Optional.of(result()));

        ResponseEntity<?> response = controller.updateUserById(1L, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateUserById_shouldReturnNotFound_whenUserDoesNotExist() {
        when(service.updateById(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateUserById(1L, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUserById_shouldReturnNoContent_whenUserExists() {
        when(service.deleteById(1L)).thenReturn(true);

        ResponseEntity<?> response = controller.deleteUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteUserById_shouldReturnNotFound_whenUserDoesNotExist() {
        when(service.deleteById(1L)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private UserRequest request() {
        return new UserRequest("adopter.demo", "Camila", "Rios", "adopter@mail.com", "secret123",
                "123456789", "Chile", "Santiago", "Calle 1", "123", "8320000", "HOME",
                UserStatus.ACTIVE, User.Role.ADOPTER, true);
    }

    private UserResult result() {
        return new UserResult(1L, "adopter.demo", "Camila", "Rios", "adopter@mail.com",
                "123456789", "Chile", "Santiago", "Calle 1", "123", "8320000", "HOME",
                UserStatus.ACTIVE, User.Role.ADOPTER, true);
    }
}