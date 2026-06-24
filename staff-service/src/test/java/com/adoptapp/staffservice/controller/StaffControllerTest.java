package com.adoptapp.staffservice.controller;

import com.adoptapp.staffservice.dto.StaffResult;
import com.adoptapp.staffservice.model.StaffPosition;
import com.adoptapp.staffservice.model.StaffStatus;
import com.adoptapp.staffservice.service.StaffLinkAssembler;
import com.adoptapp.staffservice.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    private StaffService service;

    private StaffController controller;

    @BeforeEach
    void setUp() {
        controller = new StaffController(service, new StaffLinkAssembler());
    }

    @Test
    void getStaffById_shouldReturnOk_whenStaffExists() {
        StaffResult result = new StaffResult(1L, 7L, 2L, StaffPosition.VETERINARIAN,
                "123456789", "vet@mail.com", LocalDateTime.now(), StaffStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(service.getById(1L)).thenReturn(Optional.of(result));

        ResponseEntity<?> response = controller.getStaffById(1L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getStaffById_shouldReturnNotFound_whenStaffDoesNotExist() {
        when(service.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getStaffById(99L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Authentication admin() {
        return new TestingAuthenticationToken("admin@mail.com", "password", "ROLE_ADMIN");
    }
}