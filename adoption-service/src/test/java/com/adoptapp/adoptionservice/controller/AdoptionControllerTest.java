package com.adoptapp.adoptionservice.controller;

import com.adoptapp.adoptionservice.dto.AdoptionCommand;
import com.adoptapp.adoptionservice.dto.AdoptionCreateRequest;
import com.adoptapp.adoptionservice.dto.AdoptionHistoryResponse;
import com.adoptapp.adoptionservice.dto.AdoptionResponse;
import com.adoptapp.adoptionservice.dto.AdoptionResult;
import com.adoptapp.adoptionservice.dto.AdoptionUpdateRequest;
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import com.adoptapp.adoptionservice.service.AdoptionLinkAssembler;
import com.adoptapp.adoptionservice.service.AdoptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionControllerTest {

    @Mock
    private AdoptionService service;

    private AdoptionController controller;

    @BeforeEach
    void setUp() {
        controller = new AdoptionController(service, new AdoptionLinkAssembler());
    }

    @Test
    void getAllAdoptions_shouldReturnOkForAdmin() {
        // given
        when(service.getAdoptions()).thenReturn(List.of(result()));

        // when
        ResponseEntity<CollectionModel<EntityModel<AdoptionResponse>>> response = controller.getAllAdoptions(admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().iterator().next().getContent().id()).isEqualTo(1L);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).getAdoptions();
    }

    @Test
    void getAllAdoptions_shouldReturnOnlyShelterAdoptionsForShelterAdmin() {
        // given
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(20L);
        when(service.getShelterIdForStaffUser(20L)).thenReturn(2L);
        when(service.getAdoptionsByShelter(2L)).thenReturn(List.of(result()));

        // when
        ResponseEntity<CollectionModel<EntityModel<AdoptionResponse>>> response = controller.getAllAdoptions(shelterAdmin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).getAdoptionsByShelter(2L);
    }

    @Test
    void getAdoptionById_shouldReturnOk_whenAdminFindsAdoption() {
        // given
        when(service.getById(1L)).thenReturn(Optional.of(result()));

        // when
        ResponseEntity<EntityModel<AdoptionResponse>> response = controller.getAdoptionById(1L, admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().id()).isEqualTo(1L);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).getById(1L);
    }

    @Test
    void getAdoptionById_shouldReturnNotFound_whenAdminDoesNotFindAdoption() {
        // given
        when(service.getById(99L)).thenReturn(Optional.empty());

        // when
        ResponseEntity<EntityModel<AdoptionResponse>> response = controller.getAdoptionById(99L, admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(service).getById(99L);
    }

    @Test
    void getHistory_shouldReturnOk_whenAdminRequestsHistory() {
        // given
        AdoptionHistoryResponse history = new AdoptionHistoryResponse(
                1L, 1L, "UPDATED", "Estado actualizado", LocalDateTime.now());
        when(service.getHistory(1L)).thenReturn(List.of(history));

        // when
        ResponseEntity<List<AdoptionHistoryResponse>> response = controller.getHistory(1L, admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(service).getHistory(1L);
    }

    @Test
    void createAdoption_shouldCreateWithAuthenticatedUser_whenUserIsAdopter() {
        // given
        when(service.getUserIdByEmail("adopter@mail.com")).thenReturn(7L);
        when(service.create(any(AdoptionCommand.class))).thenReturn(result());

        // when
        ResponseEntity<EntityModel<AdoptionResponse>> response = controller.createAdoption(
                new AdoptionCreateRequest(10L, 99L), adopter());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getContent().id()).isEqualTo(1L);
        assertThat(response.getBody().getLinks()).isNotEmpty();

        ArgumentCaptor<AdoptionCommand> captor = ArgumentCaptor.forClass(AdoptionCommand.class);
        verify(service).create(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(7L);
        assertThat(captor.getValue().petId()).isEqualTo(10L);
        assertThat(captor.getValue().status()).isNull();
    }

    @Test
    void createAdoption_shouldCreateForShelterAdminWithShelterRestriction() {
        // given
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(20L);
        when(service.getShelterIdForStaffUser(20L)).thenReturn(2L);
        when(service.createForShelterAdmin(any(AdoptionCommand.class), eq(2L))).thenReturn(result());

        // when
        ResponseEntity<EntityModel<AdoptionResponse>> response = controller.createAdoption(
                new AdoptionCreateRequest(10L, 7L), shelterAdmin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).createForShelterAdmin(any(AdoptionCommand.class), eq(2L));
    }

    @Test
    void updateAdoptionById_shouldUpdate_whenAdminRequestsValidStatus() {
        // given
        when(service.updateById(eq(1L), any(AdoptionCommand.class))).thenReturn(Optional.of(result()));

        // when
        ResponseEntity<EntityModel<AdoptionResponse>> response = controller.updateAdoptionById(
                1L, new AdoptionUpdateRequest(AdoptionStatus.APPROVED), admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLinks()).isNotEmpty();

        ArgumentCaptor<AdoptionCommand> captor = ArgumentCaptor.forClass(AdoptionCommand.class);
        verify(service).updateById(eq(1L), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(AdoptionStatus.APPROVED);
        assertThat(captor.getValue().userId()).isNull();
        assertThat(captor.getValue().petId()).isNull();
    }

    @Test
    void updateAdoptionById_shouldReturnNotFound_whenShelterAdminDoesNotOwnAdoption() {
        // given
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(20L);
        when(service.getShelterIdForStaffUser(20L)).thenReturn(2L);
        when(service.getByIdForShelter(1L, 2L)).thenReturn(Optional.empty());

        // when
        ResponseEntity<EntityModel<AdoptionResponse>> response = controller.updateAdoptionById(
                1L, new AdoptionUpdateRequest(AdoptionStatus.APPROVED), shelterAdmin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(service, never()).updateById(eq(1L), any(AdoptionCommand.class));
    }

    @Test
    void deleteAdoptionById_shouldReturnNoContent_whenDeleted() {
        // given
        when(service.deleteById(1L)).thenReturn(true);

        // when
        ResponseEntity<Void> response = controller.deleteAdoptionById(1L, admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteById(1L);
    }

    @Test
    void deleteAdoptionById_shouldReturnNotFound_whenAdoptionDoesNotExist() {
        // given
        when(service.deleteById(99L)).thenReturn(false);

        // when
        ResponseEntity<Void> response = controller.deleteAdoptionById(99L, admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(service).deleteById(99L);
    }

    private AdoptionResult result() {
        return new AdoptionResult(
                1L,
                7L,
                10L,
                AdoptionStatus.PENDING,
                LocalDateTime.of(2026, 6, 21, 10, 0),
                null
        );
    }

    private Authentication admin() {
        return new TestingAuthenticationToken("admin@mail.com", "password", "ROLE_ADMIN");
    }

    private Authentication shelterAdmin() {
        return new TestingAuthenticationToken("shelter@mail.com", "password", "ROLE_SHELTER_ADMIN");
    }

    private Authentication adopter() {
        return new TestingAuthenticationToken("adopter@mail.com", "password", "ROLE_ADOPTER");
    }
}