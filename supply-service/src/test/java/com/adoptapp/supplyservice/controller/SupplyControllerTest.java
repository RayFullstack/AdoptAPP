package com.adoptapp.supplyservice.controller;

import com.adoptapp.supplyservice.dto.SupplyHistoryResponse;
import com.adoptapp.supplyservice.dto.SupplyRequest;
import com.adoptapp.supplyservice.dto.SupplyResult;
import com.adoptapp.supplyservice.model.SupplyStatus;
import com.adoptapp.supplyservice.service.SupplyLinkAssembler;
import com.adoptapp.supplyservice.service.SupplyService;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyControllerTest {

    @Mock
    private SupplyService service;

    private SupplyController controller;

    @BeforeEach
    void setUp() {
        controller = new SupplyController(service, new SupplyLinkAssembler());
    }

    @Test
    void getAllSupplies_shouldReturnOkForAdminWithoutStatus() {
        when(service.getSupplies()).thenReturn(List.of(result()));

        ResponseEntity<?> response = controller.getAllSupplies(null, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).getSupplies();
    }

    @Test
    void getAllSupplies_shouldReturnShelterScopedListForShelterAdmin() {
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(10L);
        when(service.getShelterIdForStaffUser(10L)).thenReturn(2L);
        when(service.findByShelterId(2L, "AVAILABLE")).thenReturn(List.of(result()));

        ResponseEntity<?> response = controller.getAllSupplies("AVAILABLE", shelterAdmin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).findByShelterId(2L, "AVAILABLE");
    }

    @Test
    void getSupplyById_shouldReturnOk_whenSupplyExists() {
        when(service.getById(1L)).thenReturn(Optional.of(result()));

        ResponseEntity<?> response = controller.getSupplyById(1L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getSupplyById_shouldReturnNotFound_whenSupplyDoesNotExist() {
        when(service.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getSupplyById(99L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getSuppliesByShelter_shouldReturnOk_whenAdminRequestsAnyShelter() {
        when(service.findByShelterId(2L)).thenReturn(List.of(result()));

        ResponseEntity<?> response = controller.getSuppliesByShelter(2L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getSuppliesByShelter_shouldReturnNotFound_whenShelterAdminRequestsAnotherShelter() {
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(10L);
        when(service.getShelterIdForStaffUser(10L)).thenReturn(99L);

        ResponseEntity<?> response = controller.getSuppliesByShelter(2L, shelterAdmin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getActiveSuppliesByShelter_shouldReturnOk() {
        when(service.findByShelterId(2L)).thenReturn(List.of(result()));

        ResponseEntity<?> response = controller.getActiveSuppliesByShelter(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getSupplyHistory_shouldReturnOk_whenHistoryExists() {
        SupplyHistoryResponse history = new SupplyHistoryResponse(1L, 1L, "UPDATED", "Cambio", "AVAILABLE",
                "LOW_STOCK", 10, 2, "FOOD", "FOOD", 1L, LocalDateTime.now());
        when(service.getHistory(1L)).thenReturn(Optional.of(List.of(history)));

        ResponseEntity<?> response = controller.getSupplyHistory(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createSupply_shouldReturnCreated_whenAdminCreates() {
        when(service.create(any())).thenReturn(result());

        ResponseEntity<?> response = controller.createSupply(request(2L), admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createSupply_shouldReturnNotFound_whenShelterAdminUsesAnotherShelter() {
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(10L);
        when(service.getShelterIdForStaffUser(10L)).thenReturn(99L);

        ResponseEntity<?> response = controller.createSupply(request(2L), shelterAdmin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateSupply_shouldReturnOk_whenAdminUpdates() {
        when(service.getById(1L)).thenReturn(Optional.of(result()));
        when(service.update(any(), any())).thenReturn(Optional.of(result()));

        ResponseEntity<?> response = controller.updateSupply(1L, request(2L), admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateSupply_shouldReturnNotFound_whenSupplyIsNotVisible() {
        when(service.getById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateSupply(1L, request(2L), admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteSupply_shouldReturnNoContent_whenDeleted() {
        when(service.getById(1L)).thenReturn(Optional.of(result()));
        when(service.delete(1L)).thenReturn(true);

        ResponseEntity<?> response = controller.deleteSupply(1L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteSupply_shouldReturnNotFound_whenSupplyDoesNotExist() {
        when(service.getById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.deleteSupply(1L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private SupplyRequest request(Long shelterId) {
        return new SupplyRequest("Alimento", "Saco", 10, "kg", "FOOD", shelterId, 1L,
                "Proveedor", 2, SupplyStatus.AVAILABLE);
    }

    private SupplyResult result() {
        return new SupplyResult(1L, "Alimento", "Saco", 10, "kg", "FOOD", 2L,
                "Proveedor", 2, "AVAILABLE", LocalDateTime.now(), LocalDateTime.now());
    }

    private Authentication admin() {
        return new TestingAuthenticationToken("admin@mail.com", "password", "ROLE_ADMIN");
    }

    private Authentication shelterAdmin() {
        return new TestingAuthenticationToken("shelter@mail.com", "password", "ROLE_SHELTER_ADMIN");
    }
}