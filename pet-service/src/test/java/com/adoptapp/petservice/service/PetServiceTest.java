package com.adoptapp.petservice.service;

import com.adoptapp.petservice.client.HealthServiceClient;
import com.adoptapp.petservice.client.NotificationServiceClient;
import com.adoptapp.petservice.client.ShelterServiceClient;
import com.adoptapp.petservice.client.StaffServiceClient;
import com.adoptapp.petservice.client.UserServiceClient;
import com.adoptapp.petservice.dto.HealthResult;
import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.dto.ShelterResponse;
import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.model.PetHistory;
import com.adoptapp.petservice.model.PetStatus;
import com.adoptapp.petservice.repository.PetHistoryRepository;
import com.adoptapp.petservice.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository repository;

    @Mock
    private PetHistoryRepository historyRepository;

    @Mock
    private HealthServiceClient healthServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private ShelterServiceClient shelterServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private StaffServiceClient staffServiceClient;

    @InjectMocks
    private PetService service;

    @Test
    void create_shouldCreatePetAvailable_whenDataIsValid() {
        // given
        PetCommand command = command("AVAILABLE", 2L);
        Pet savedPet = pet(1L, PetStatus.AVAILABLE, 2L);

        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(shelter(2L)));
        when(repository.save(any(Pet.class)))
                .thenReturn(savedPet);
        when(repository.findById(1L))
                .thenReturn(Optional.of(savedPet));

        // when
        PetResult result = service.create(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Cholito");
        assertThat(result.status()).isEqualTo("AVAILABLE");
        assertThat(result.shelterId()).isEqualTo(2L);

        verify(shelterServiceClient, times(2)).getShelterById(2L);
        verify(repository).save(any(Pet.class));
        verify(historyRepository).save(any(PetHistory.class));
    }

    @Test
    void create_shouldThrow_whenShelterDoesNotExist() {
        // given
        PetCommand command = command("AVAILABLE", 2L);

        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.notFound().build());

        // when / then
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El refugio con ID 2 no existe");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void create_shouldThrow_whenStatusIsDeleted() {
        // given
        PetCommand command = command("DELETED", 2L);

        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(shelter(2L)));

        // when / then
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado de mascota invalido: DELETED");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void createForShelter_shouldThrow_whenShelterDoesNotBelongToAuthenticatedStaff() {
        // given
        PetCommand command = command("AVAILABLE", 3L);

        when(shelterServiceClient.getShelterById(3L))
                .thenReturn(ResponseEntity.ok(shelter(3L)));

        // when / then
        assertThatThrownBy(() -> service.createForShelter(command, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La mascota no pertenece al refugio del usuario autenticado");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void getPets_shouldHideDeletedPets_whenNoStatusFilterIsUsed() {
        // given
        Pet available = pet(1L, PetStatus.AVAILABLE, 2L);
        Pet notAvailable = pet(2L, PetStatus.NOT_AVAILABLE, 2L);

        when(repository.findByStatusInOrderByCreatedAtAsc(anyList()))
                .thenReturn(List.of(available, notAvailable));

        // when
        List<PetResult> result = service.getPets();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PetResult::status)
                .containsExactly("AVAILABLE", "NOT_AVAILABLE");
        verify(repository).findByStatusInOrderByCreatedAtAsc(
                List.of(PetStatus.AVAILABLE, PetStatus.NOT_AVAILABLE));
    }

    @Test
    void getPets_shouldThrow_whenStatusFilterIsInvalid() {
        // when / then
        assertThatThrownBy(() -> service.getPets("BAD_STATUS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status invalido: BAD_STATUS");
    }

    @Test
    void getById_shouldReturnEmpty_whenPetIsDeleted() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.DELETED, 2L)));

        // when
        Optional<PetResult> result = service.getById(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getByIdForShelter_shouldReturnEmpty_whenPetBelongsToAnotherShelter() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.AVAILABLE, 3L)));

        // when
        Optional<PetResult> result = service.getByIdForShelter(1L, 2L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void updateById_shouldUpdatePet_whenDataIsValid() {
        // given
        Pet existing = pet(1L, PetStatus.AVAILABLE, 2L);
        PetCommand command = command("NOT_AVAILABLE", 2L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(shelter(2L)));
        when(repository.save(any(Pet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Optional<PetResult> result = service.updateById(1L, command);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo("NOT_AVAILABLE");
        verify(repository).save(existing);
        verify(historyRepository).save(any(PetHistory.class));
    }

    @Test
    void updateById_shouldReturnEmpty_whenPetDoesNotExist() {
        // given
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        // when
        Optional<PetResult> result = service.updateById(99L, command("AVAILABLE", 2L));

        // then
        assertThat(result).isEmpty();
        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void updateById_shouldThrow_whenPetIsDeleted() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.DELETED, 2L)));
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(shelter(2L)));

        // when / then
        assertThatThrownBy(() -> service.updateById(1L, command("AVAILABLE", 2L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede actualizar una mascota eliminada");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void updateByIdForShelter_shouldThrow_whenExistingPetBelongsToAnotherShelter() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.AVAILABLE, 3L)));
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(shelter(2L)));

        // when / then
        assertThatThrownBy(() -> service.updateByIdForShelter(1L, command("AVAILABLE", 2L), 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La mascota no pertenece al refugio del usuario autenticado");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void updateByStatus_shouldUpdateStatus_whenPetIsActive() {
        // given
        Pet pet = pet(1L, PetStatus.AVAILABLE, 2L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(pet));
        when(repository.save(any(Pet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Optional<PetResult> result = service.updateByStatus(1L, "NOT_AVAILABLE");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo("NOT_AVAILABLE");
        verify(repository).save(pet);
        verify(historyRepository).save(any(PetHistory.class));
    }

    @Test
    void updateByStatus_shouldThrow_whenPetIsDeleted() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.DELETED, 2L)));

        // when / then
        assertThatThrownBy(() -> service.updateByStatus(1L, "AVAILABLE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede cambiar el estado de una mascota eliminada");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void updateByStatus_shouldThrow_whenStatusIsDeleted() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.AVAILABLE, 2L)));

        // when / then
        assertThatThrownBy(() -> service.updateByStatus(1L, "DELETED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado de mascota invalido: DELETED");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void deleteById_shouldMarkPetDeleted_whenPetIsActiveAndHealthIsDeleted() {
        // given
        Pet pet = pet(1L, PetStatus.AVAILABLE, 2L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(pet));
        when(healthServiceClient.deleteHealthByPetId(1L))
                .thenReturn(ResponseEntity.noContent().build());
        when(repository.save(any(Pet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        boolean result = service.deleteById(1L);

        // then
        assertThat(result).isTrue();
        assertThat(pet.getStatus()).isEqualTo(PetStatus.DELETED);
        verify(healthServiceClient).deleteHealthByPetId(1L);
        verify(repository).save(pet);
        verify(historyRepository).save(any(PetHistory.class));
    }

    @Test
    void deleteById_shouldReturnFalse_whenPetDoesNotExist() {
        // given
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        // when
        boolean result = service.deleteById(99L);

        // then
        assertThat(result).isFalse();
        verify(healthServiceClient, never()).deleteHealthByPetId(any());
        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void deleteById_shouldReturnFalse_whenPetIsAlreadyDeleted() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.DELETED, 2L)));

        // when
        boolean result = service.deleteById(1L);

        // then
        assertThat(result).isFalse();
        verify(healthServiceClient, never()).deleteHealthByPetId(any());
        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void deleteById_shouldThrow_whenHealthServiceCannotDeleteAssociatedHealth() {
        // given
        when(repository.findById(1L))
                .thenReturn(Optional.of(pet(1L, PetStatus.AVAILABLE, 2L)));
        when(healthServiceClient.deleteHealthByPetId(1L))
                .thenReturn(ResponseEntity.internalServerError().build());

        // when / then
        assertThatThrownBy(() -> service.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo eliminar la ficha de salud asociada a la mascota 1");

        verify(repository, never()).save(any(Pet.class));
    }

    @Test
    void getHealthInfo_shouldReturnHealth_whenPetIsActiveAndHealthExists() {
        // given
        HealthResult health = new HealthResult(
                5L,
                1L,
                10L,
                "VACCINATED",
                "STERILIZED",
                "Sin enfermedades",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(repository.findById(10L))
                .thenReturn(Optional.of(pet(10L, PetStatus.AVAILABLE, 2L)));
        when(healthServiceClient.getHealthByPetId(10L))
                .thenReturn(ResponseEntity.ok(health));

        // when
        Optional<HealthResult> result = service.getHealthInfo(10L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().petId()).isEqualTo(10L);
    }

    @Test
    void getHealthInfo_shouldReturnEmpty_whenPetIsDeleted() {
        // given
        when(repository.findById(10L))
                .thenReturn(Optional.of(pet(10L, PetStatus.DELETED, 2L)));

        // when
        Optional<HealthResult> result = service.getHealthInfo(10L);

        // then
        assertThat(result).isEmpty();
        verify(healthServiceClient, never()).getHealthByPetId(any());
    }

    @Test
    void getPetsByShelter_shouldHideDeletedPetsForShelter() {
        // given
        when(repository.findByShelterIdAndStatusInOrderByCreatedAtAsc(eq(2L), anyList()))
                .thenReturn(List.of(pet(1L, PetStatus.AVAILABLE, 2L)));

        // when
        List<PetResult> result = service.getPetsByShelter(2L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().shelterId()).isEqualTo(2L);
        verify(repository).findByShelterIdAndStatusInOrderByCreatedAtAsc(
                2L,
                List.of(PetStatus.AVAILABLE, PetStatus.NOT_AVAILABLE));
    }

    private PetCommand command(String status, Long shelterId) {
        return new PetCommand(
                "Cholito",
                "Perro",
                "Mestizo",
                3,
                "MEDIUM",
                "Cafe",
                "Tranquilo",
                status,
                shelterId
        );
    }

    private Pet pet(Long id, PetStatus status, Long shelterId) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName("Cholito");
        pet.setSpecies("Perro");
        pet.setRace("Mestizo");
        pet.setAge(3);
        pet.setSize("MEDIUM");
        pet.setColor("Cafe");
        pet.setPersonality("Tranquilo");
        pet.setShelterId(shelterId);
        pet.setStatus(status);
        return pet;
    }

    private ShelterResponse shelter(Long id) {
        return new ShelterResponse(id, "refugio@mail.com");
    }
}
