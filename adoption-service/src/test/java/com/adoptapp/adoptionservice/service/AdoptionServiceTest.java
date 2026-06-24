package com.adoptapp.adoptionservice.service;

import com.adoptapp.adoptionservice.client.FollowUpServiceClient;
import com.adoptapp.adoptionservice.client.PetNotificationClient;
import com.adoptapp.adoptionservice.client.PetServiceClient;
import com.adoptapp.adoptionservice.client.ShelterServiceClient;
import com.adoptapp.adoptionservice.client.StaffServiceClient;
import com.adoptapp.adoptionservice.client.UserNotificationClient;
import com.adoptapp.adoptionservice.client.UserServiceClient;
import com.adoptapp.adoptionservice.dto.AdoptionCommand;
import com.adoptapp.adoptionservice.dto.AdoptionResult;
import com.adoptapp.adoptionservice.dto.FollowUpRequest;
import com.adoptapp.adoptionservice.dto.PetNotificationRequest;
import com.adoptapp.adoptionservice.dto.PetResponse;
import com.adoptapp.adoptionservice.dto.PetStatusRequest;
import com.adoptapp.adoptionservice.dto.ShelterResponse;
import com.adoptapp.adoptionservice.dto.UserNotificationRequest;
import com.adoptapp.adoptionservice.dto.UserResponse;
import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.model.AdoptionHistory;
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import com.adoptapp.adoptionservice.repository.AdoptionHistoryRepository;
import com.adoptapp.adoptionservice.repository.AdoptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

    @Mock
    private AdoptionRepository repository;

    @Mock
    private AdoptionHistoryRepository historyRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PetServiceClient petServiceClient;

    @Mock
    private ShelterServiceClient shelterServiceClient;

    @Mock
    private StaffServiceClient staffServiceClient;

    @Mock
    private FollowUpServiceClient followUpServiceClient;

    @Mock
    private UserNotificationClient userNotificationClient;

    @Mock
    private PetNotificationClient petNotificationClient;

    @InjectMocks
    private AdoptionService service;

    @Test
    void create_shouldCreatePendingAdoption_whenDataIsValid() {
        // given
        AdoptionCommand command = command(AdoptionStatus.PENDING);
        stubValidUserPetAndShelter();
        when(repository.existsByPetIdAndStatusIn(eq(10L), anyList()))
                .thenReturn(false);
        when(repository.save(any(Adoption.class)))
                .thenAnswer(invocation -> savedAdoption(invocation.getArgument(0), 100L));

        // when
        AdoptionResult result = service.create(command);

        // then
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.petId()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(AdoptionStatus.PENDING);
        verify(repository).save(any(Adoption.class));
        verify(historyRepository).save(any(AdoptionHistory.class));
        verify(userNotificationClient).sendNotification(any(UserNotificationRequest.class));
    }

    @Test
    void create_shouldThrow_whenUserDoesNotExist() {
        // given
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.notFound().build());

        // when / then
        assertThatThrownBy(() -> service.create(command(AdoptionStatus.PENDING)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario con ID 1 no existe");
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(petServiceClient, shelterServiceClient, historyRepository);
    }

    @Test
    void create_shouldThrow_whenPetDoesNotExist() {
        // given
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L))
                .thenReturn(ResponseEntity.notFound().build());

        // when / then
        assertThatThrownBy(() -> service.create(command(AdoptionStatus.PENDING)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mascota con ID 10 no existe");
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(shelterServiceClient, historyRepository);
    }

    @Test
    void create_shouldThrow_whenPetIsNotAvailable() {
        // given
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L))
                .thenReturn(ResponseEntity.ok(pet("NOT_AVAILABLE", 2L)));

        // when / then
        assertThatThrownBy(() -> service.create(command(AdoptionStatus.PENDING)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mascota con ID 10 no esta disponible para adopcion");
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(shelterServiceClient, historyRepository);
    }

    @Test
    void create_shouldThrow_whenPetHasNoShelterId() {
        // given
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L))
                .thenReturn(ResponseEntity.ok(pet("AVAILABLE", null)));

        // when / then
        assertThatThrownBy(() -> service.create(command(AdoptionStatus.PENDING)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mascota con ID 10 no tiene refugio asociado");
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(shelterServiceClient, historyRepository);
    }

    @Test
    void create_shouldThrow_whenActiveAdoptionAlreadyExists() {
        // given
        stubValidUserPetAndShelter();
        when(repository.existsByPetIdAndStatusIn(eq(10L), anyList()))
                .thenReturn(true);

        // when / then
        assertThatThrownBy(() -> service.create(command(AdoptionStatus.PENDING)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mascota ya tiene una adopcion pendiente o aprobada");
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(historyRepository);
    }

    @Test
    void createForShelterAdmin_shouldThrow_whenPetBelongsToAnotherShelter() {
        // given
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L))
                .thenReturn(ResponseEntity.ok(pet("AVAILABLE", 3L)));
        when(shelterServiceClient.getShelterById(3L))
                .thenReturn(ResponseEntity.ok(new ShelterResponse(3L, "otro@mail.com")));

        // when / then
        assertThatThrownBy(() -> service.createForShelterAdmin(command(AdoptionStatus.PENDING), 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mascota con ID 10 no pertenece al refugio del usuario autenticado");
        verify(repository, never()).save(any(Adoption.class));
    }

    @Test
    void updateById_shouldApproveAdoptionAndSyncPet_whenDataIsValid() {
        // given
        Adoption adoption = adoption(100L, AdoptionStatus.PENDING);
        when(repository.findById(100L)).thenReturn(Optional.of(adoption));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet("AVAILABLE", 2L)));
        when(repository.existsByPetIdAndStatusInAndIdNot(eq(10L), anyList(), eq(100L)))
                .thenReturn(false);
        when(petServiceClient.updatePetStatus(eq(10L), any(PetStatusRequest.class)))
                .thenReturn(ResponseEntity.ok(pet("NOT_AVAILABLE", 2L)));
        when(repository.save(any(Adoption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Optional<AdoptionResult> result = service.updateById(100L, command(AdoptionStatus.APPROVED));

        // then
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(AdoptionStatus.APPROVED);
        verify(petServiceClient).updatePetStatus(eq(10L), argThat(request -> request.status().equals("NOT_AVAILABLE")));
        verify(followUpServiceClient).createFollowUp(any(FollowUpRequest.class));
        verify(historyRepository).save(any(AdoptionHistory.class));
    }

    @Test
    void updateById_shouldRejectAdoptionWithoutSyncingPet_whenPending() {
        // given
        Adoption adoption = adoption(100L, AdoptionStatus.PENDING);
        when(repository.findById(100L)).thenReturn(Optional.of(adoption));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet("AVAILABLE", 2L)));
        when(repository.save(any(Adoption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Optional<AdoptionResult> result = service.updateById(100L, command(AdoptionStatus.REJECTED));

        // then
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(AdoptionStatus.REJECTED);
        verify(petServiceClient, never()).updatePetStatus(anyLong(), any(PetStatusRequest.class));
        verifyNoInteractions(followUpServiceClient);
    }

    @Test
    void updateById_shouldThrow_whenAdoptionIsCancelled() {
        // given
        when(repository.findById(100L))
                .thenReturn(Optional.of(adoption(100L, AdoptionStatus.CANCELLED)));

        // when / then
        assertThatThrownBy(() -> service.updateById(100L, command(AdoptionStatus.APPROVED)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("NO SE PUEDE ACTUALIZAR UNA ADOPCION CANCELADA");
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(userServiceClient, petServiceClient, historyRepository);
    }

    @Test
    void updateById_shouldThrow_whenTransitionIsInvalid() {
        // given
        when(repository.findById(100L))
                .thenReturn(Optional.of(adoption(100L, AdoptionStatus.APPROVED)));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet("NOT_AVAILABLE", 2L)));

        // when / then
        assertThatThrownBy(() -> service.updateById(100L, command(AdoptionStatus.REJECTED)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transicion de estado no permitida: APPROVED -> REJECTED");
        verify(repository, never()).save(any(Adoption.class));
    }

    @Test
    void updateById_shouldReturnEmpty_whenAdoptionDoesNotExist() {
        // given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // when
        Optional<AdoptionResult> result = service.updateById(99L, command(AdoptionStatus.APPROVED));

        // then
        assertThat(result).isEmpty();
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(userServiceClient, petServiceClient, historyRepository);
    }

    @Test
    void updateById_shouldThrow_whenPetStatusSynchronizationFails() {
        // given
        Adoption adoption = adoption(100L, AdoptionStatus.PENDING);
        when(repository.findById(100L)).thenReturn(Optional.of(adoption));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet("AVAILABLE", 2L)));
        when(repository.existsByPetIdAndStatusInAndIdNot(eq(10L), anyList(), eq(100L)))
                .thenReturn(false);
        when(petServiceClient.updatePetStatus(eq(10L), any(PetStatusRequest.class)))
                .thenThrow(new RuntimeException("pet-service no disponible"));

        // when / then
        assertThatThrownBy(() -> service.updateById(100L, command(AdoptionStatus.APPROVED)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al actualizar adopcion: no se pudo completar la validacion");
        assertThat(adoption.getStatus()).isEqualTo(AdoptionStatus.PENDING);
        verify(repository, never()).save(any(Adoption.class));
        verifyNoInteractions(historyRepository, followUpServiceClient);
    }

    @Test
    void deleteById_shouldCancelAdoption_whenAdoptionIsPending() {
        // given
        Adoption adoption = adoption(100L, AdoptionStatus.PENDING);
        when(repository.findById(100L)).thenReturn(Optional.of(adoption));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(user()));
        when(repository.save(any(Adoption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        boolean result = service.deleteById(100L);

        // then
        assertThat(result).isTrue();
        assertThat(adoption.getStatus()).isEqualTo(AdoptionStatus.CANCELLED);
        verify(repository).save(adoption);
        verify(petServiceClient, never()).updatePetStatus(anyLong(), any(PetStatusRequest.class));
        verify(historyRepository).save(any(AdoptionHistory.class));
    }

    @Test
    void deleteById_shouldMakePetAvailable_whenApprovedAdoptionIsCancelled() {
        // given
        Adoption adoption = adoption(100L, AdoptionStatus.APPROVED);
        when(repository.findById(100L)).thenReturn(Optional.of(adoption));
        when(repository.existsByPetIdAndStatusInAndIdNot(eq(10L), eq(List.of(AdoptionStatus.APPROVED)), eq(100L)))
                .thenReturn(false);
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.updatePetStatus(eq(10L), any(PetStatusRequest.class)))
                .thenReturn(ResponseEntity.ok(pet("AVAILABLE", 2L)));
        when(repository.save(any(Adoption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        boolean result = service.deleteById(100L);

        // then
        assertThat(result).isTrue();
        verify(petServiceClient).updatePetStatus(eq(10L), argThat(request -> request.status().equals("AVAILABLE")));
        assertThat(adoption.getStatus()).isEqualTo(AdoptionStatus.CANCELLED);
    }

    @Test
    void getById_shouldReturnEmpty_whenAdoptionIsCancelled() {
        // given
        when(repository.findById(100L))
                .thenReturn(Optional.of(adoption(100L, AdoptionStatus.CANCELLED)));

        // when
        Optional<AdoptionResult> result = service.getById(100L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getByIdForShelter_shouldReturnAdoption_whenPetBelongsToShelter() {
        // given
        when(repository.findById(100L))
                .thenReturn(Optional.of(adoption(100L, AdoptionStatus.PENDING)));
        when(petServiceClient.getPetById(10L))
                .thenReturn(ResponseEntity.ok(pet("AVAILABLE", 2L)));

        // when
        Optional<AdoptionResult> result = service.getByIdForShelter(100L, 2L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(100L);
        assertThat(result.get().petId()).isEqualTo(10L);
    }

    private void stubValidUserPetAndShelter() {
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(user()));
        when(petServiceClient.getPetById(10L))
                .thenReturn(ResponseEntity.ok(pet("AVAILABLE", 2L)));
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
    }

    private AdoptionCommand command(AdoptionStatus status) {
        return new AdoptionCommand(1L, 10L, status);
    }

    private Adoption adoption(Long id, AdoptionStatus status) {
        Adoption adoption = new Adoption();
        adoption.setId(id);
        adoption.setUserId(1L);
        adoption.setPetId(10L);
        adoption.setStatus(status);
        return adoption;
    }

    private Adoption savedAdoption(Adoption adoption, Long id) {
        adoption.setId(id);
        return adoption;
    }

    private UserResponse user() {
        return new UserResponse(1L, "adopter@mail.com");
    }

    private PetResponse pet(String status, Long shelterId) {
        return new PetResponse(
                10L,
                "Benito",
                "Perro",
                "Samoyedo",
                3,
                "MEDIUM",
                "Blanco",
                status,
                "Amoroso",
                shelterId
        );
    }
}