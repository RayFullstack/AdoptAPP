package com.adoptapp.petservice.controller;

import com.adoptapp.petservice.dto.HealthResult;
import com.adoptapp.petservice.dto.PetHistoryResult;
import com.adoptapp.petservice.dto.PetRequest;
import com.adoptapp.petservice.dto.PetResponse;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.dto.PetStatusRequest;
import com.adoptapp.petservice.service.PetLinkAssembler;
import com.adoptapp.petservice.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    @Mock
    private PetService service;

    private PetController controller;

    @BeforeEach
    void setUp() {
        controller = new PetController(service, new PetLinkAssembler());
    }

    @Test
    void getAllPets_shouldReturnOk() {
        // given
        when(service.getPets()).thenReturn(List.of(result()));

        // when
        ResponseEntity<CollectionModel<EntityModel<PetResponse>>> response = controller.getAllPets();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().iterator().next().getContent().name()).isEqualTo("Benito");
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).getPets();
    }

    @Test
    void getActivePetsByShelter_shouldReturnOk() {
        // given
        when(service.getPetsByShelter(2L)).thenReturn(List.of(result()));

        // when
        ResponseEntity<CollectionModel<EntityModel<PetResponse>>> response = controller.getActivePetsByShelter(2L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().iterator().next().getContent().shelterId()).isEqualTo(2L);
        assertThat(response.getBody().getLinks()).isNotEmpty();
    }

    @Test
    void getPetHealth_shouldReturnOk_whenHealthExists() {
        // given
        HealthResult health = new HealthResult(5L, 7L, 1L, "VACCINATED", "STERILIZED", "Sin enfermedades",
                LocalDateTime.now(), LocalDateTime.now());
        when(service.getHealthInfo(1L)).thenReturn(Optional.of(health));

        // when
        ResponseEntity<HealthResult> response = controller.getPetHealth(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().petId()).isEqualTo(1L);
    }

    @Test
    void getPetHealth_shouldReturnNotFound_whenHealthDoesNotExist() {
        // given
        when(service.getHealthInfo(1L)).thenReturn(Optional.empty());

        // when
        ResponseEntity<HealthResult> response = controller.getPetHealth(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPetById_shouldReturnOk_whenPetExists() {
        // given
        when(service.getById(1L)).thenReturn(Optional.of(result()));

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.getPetById(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().id()).isEqualTo(1L);
        assertThat(response.getBody().getLinks()).isNotEmpty();
    }

    @Test
    void getPetById_shouldReturnNotFound_whenPetDoesNotExist() {
        // given
        when(service.getById(99L)).thenReturn(Optional.empty());

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.getPetById(99L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getByIdIncludingDeleted_shouldUseAdminMethod_whenUserIsAdmin() {
        // given
        when(service.getByIdIncludingDeleted(1L)).thenReturn(Optional.of(result()));

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.getByIdIncludingDeleted(1L, admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).getByIdIncludingDeleted(1L);
    }

    @Test
    void getByIdIncludingDeleted_shouldUseShelterScopedMethod_whenUserIsShelterAdmin() {
        // given
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(10L);
        when(service.getShelterIdForStaffUser(10L)).thenReturn(2L);
        when(service.getByIdIncludingDeletedForShelter(1L, 2L)).thenReturn(Optional.of(result()));

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.getByIdIncludingDeleted(1L, shelterAdmin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).getByIdIncludingDeletedForShelter(1L, 2L);
    }

    @Test
    void getPetsByStatusAdmin_shouldUseStatusFilterForAdmin() {
        // given
        when(service.getPets("DELETED")).thenReturn(List.of(result()));

        // when
        ResponseEntity<CollectionModel<EntityModel<PetResponse>>> response = controller.getPetsByStatusAdmin("DELETED", admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).getPets("DELETED");
    }

    @Test
    void create_shouldUseCreateForAdmin() {
        // given
        when(service.create(any())).thenReturn(result());

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.create(request(), admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).create(any());
    }

    @Test
    void create_shouldUseCreateForShelter_whenUserIsShelterAdmin() {
        // given
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(10L);
        when(service.getShelterIdForStaffUser(10L)).thenReturn(2L);
        when(service.createForShelter(any(), any())).thenReturn(result());

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.create(request(), shelterAdmin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).createForShelter(any(), any());
    }

    @Test
    void updatePetById_shouldReturnOk_whenAdminUpdates() {
        // given
        when(service.updateById(any(), any())).thenReturn(Optional.of(result()));

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.updatePetById(1L, request(), admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLinks()).isNotEmpty();
    }

    @Test
    void updatePetById_shouldReturnNotFound_whenShelterDoesNotOwnPet() {
        // given
        when(service.getUserIdByEmail("shelter@mail.com")).thenReturn(10L);
        when(service.getShelterIdForStaffUser(10L)).thenReturn(99L);

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.updatePetById(1L, request(), shelterAdmin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatePetByStatus_shouldReturnOk_whenPetIsVisible() {
        // given
        when(service.updateByStatus(1L, "NOT_AVAILABLE")).thenReturn(Optional.of(result()));

        // when
        ResponseEntity<EntityModel<PetResponse>> response = controller.updatePetByStatus(
                1L, new PetStatusRequest("NOT_AVAILABLE"), admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLinks()).isNotEmpty();
        verify(service).updateByStatus(1L, "NOT_AVAILABLE");
    }

    @Test
    void deletePetById_shouldReturnNoContent_whenDeleted() {
        // given
        when(service.deleteById(1L)).thenReturn(true);

        // when
        ResponseEntity<Void> response = controller.deletePetById(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deletePetById_shouldReturnNotFound_whenPetDoesNotExist() {
        // given
        when(service.deleteById(1L)).thenReturn(false);

        // when
        ResponseEntity<Void> response = controller.deletePetById(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getHistory_shouldReturnOk_whenAdminCanViewPet() {
        // given
        PetHistoryResult history = new PetHistoryResult(1L, 1L, "Benito", "Benito", "AVAILABLE", "NOT_AVAILABLE",
                10L, LocalDateTime.now(), "UPDATED");
        when(service.getHistory(1L)).thenReturn(Optional.of(List.of(history)));

        // when
        ResponseEntity<List<PetHistoryResult>> response = controller.getHistory(1L, admin());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    private PetRequest request() {
        return new PetRequest("Benito", "Perro", "Mestizo", 3, "MEDIUM", "Cafe", "Tranquilo", "AVAILABLE", 2L);
    }

    private PetResult result() {
        return new PetResult(1L, "Benito", "Perro", "Mestizo", 3, "MEDIUM", "Cafe", "AVAILABLE", "Tranquilo", 2L);
    }

    private Authentication admin() {
        return new TestingAuthenticationToken("admin@mail.com", "password", "ROLE_ADMIN");
    }

    private Authentication shelterAdmin() {
        return new TestingAuthenticationToken("shelter@mail.com", "password", "ROLE_SHELTER_ADMIN");
    }
}