package com.adoptapp.shelterservice.service;

import com.adoptapp.shelterservice.client.NotificationServiceClient;
import com.adoptapp.shelterservice.client.PetServiceClient;
import com.adoptapp.shelterservice.client.StaffServiceClient;
import com.adoptapp.shelterservice.client.SupplyServiceClient;
import com.adoptapp.shelterservice.client.UserServiceClient;
import com.adoptapp.shelterservice.dto.ShelterCommand;
import com.adoptapp.shelterservice.dto.PetResponse;
import com.adoptapp.shelterservice.dto.ShelterResult;
import com.adoptapp.shelterservice.dto.StaffResponse;
import com.adoptapp.shelterservice.dto.SupplyResponse;
import com.adoptapp.shelterservice.dto.UserResponse;
import com.adoptapp.shelterservice.model.Shelter;
import com.adoptapp.shelterservice.model.ShelterStatus;
import com.adoptapp.shelterservice.repository.ShelterRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShelterServiceTest {

    @Mock private ShelterRepository repository;
    @Mock private ShelterHistoryService historyService;
    @Mock private UserServiceClient userServiceClient;
    @Mock private StaffServiceClient staffServiceClient;
    @Mock private PetServiceClient petServiceClient;
    @Mock private SupplyServiceClient supplyServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private ShelterService service;

    @Test
    void create_shouldCreateActiveShelter_whenDataIsValid() {
        ShelterCommand command = command(ShelterStatus.ACTIVE);
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "admin@mail.com")));
        when(repository.save(any(Shelter.class))).thenAnswer(invocation -> {
            Shelter shelter = invocation.getArgument(0);
            shelter.setId(2L);
            return shelter;
        });

        ShelterResult result = service.create(command, 1L);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Refugio Central");
        assertThat(result.status()).isEqualTo(ShelterStatus.ACTIVE);
        assertThat(result.active()).isTrue();
        verify(historyService).recordHistory(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_shouldThrow_whenUserDoesNotExist() {
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command(ShelterStatus.ACTIVE), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usuario con ID 1 no existe");
    }

    @Test
    void getShelters_shouldHideDeletedShelters() {
        Shelter shelter = shelter(ShelterStatus.ACTIVE, true);
        when(repository.findByStatusNot(ShelterStatus.DELETED)).thenReturn(List.of(shelter));

        List<ShelterResult> result = service.getShelters();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(ShelterStatus.ACTIVE);
    }

    @Test
    void getByIdActive_shouldReturnEmpty_whenShelterIsDeleted() {
        when(repository.findById(2L)).thenReturn(Optional.of(shelter(ShelterStatus.DELETED, false)));

        assertThat(service.getByIdActive(2L)).isEmpty();
    }


    @Test
    void getShelters_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.getShelters("wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status invalido: wrong");
    }

    @Test
    void updateById_shouldUpdateShelter_whenDataIsValid() {
        Shelter shelter = shelter(ShelterStatus.ACTIVE, true);
        ShelterCommand command = new ShelterCommand("Refugio Norte", "norte@mail.com", "987654321",
                "Rescate y adopcion", ShelterStatus.ACTIVE);

        when(repository.findById(2L)).thenReturn(Optional.of(shelter));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "admin@mail.com")));
        when(repository.save(any(Shelter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ShelterResult> result = service.updateById(2L, command, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Refugio Norte");
        assertThat(result.get().email()).isEqualTo("norte@mail.com");
        verify(historyService).recordHistory(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateById_shouldReturnEmpty_whenShelterDoesNotExist() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.updateById(2L, command(ShelterStatus.ACTIVE), 1L)).isEmpty();
    }

    @Test
    void updateById_shouldThrow_whenShelterIsDeleted() {
        when(repository.findById(2L)).thenReturn(Optional.of(shelter(ShelterStatus.DELETED, false)));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "admin@mail.com")));

        assertThatThrownBy(() -> service.updateById(2L, command(ShelterStatus.ACTIVE), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede actualizar un refugio eliminado");
    }

    @Test
    void updateById_shouldThrow_whenCommandTriesToSetDeleted() {
        when(repository.findById(2L)).thenReturn(Optional.of(shelter(ShelterStatus.ACTIVE, true)));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "admin@mail.com")));

        assertThatThrownBy(() -> service.updateById(2L, command(ShelterStatus.DELETED), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede marcar un refugio como eliminado desde update; use delete");
    }

    @Test
    void deleteById_shouldMarkShelterDeleted_whenNoActiveDependencies() {
        Shelter shelter = shelter(ShelterStatus.ACTIVE, true);
        when(repository.findById(2L)).thenReturn(Optional.of(shelter));
        when(petServiceClient.getActivePetsByShelter(2L)).thenReturn(ResponseEntity.ok(List.of()));
        when(staffServiceClient.getActiveStaffByShelter(2L)).thenReturn(ResponseEntity.ok(List.of()));
        when(supplyServiceClient.getActiveSuppliesByShelter(2L)).thenReturn(ResponseEntity.ok(List.of()));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "admin@mail.com")));
        when(repository.save(any(Shelter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.deleteById(2L, 1L);

        assertThat(result).isTrue();
        assertThat(shelter.getStatus()).isEqualTo(ShelterStatus.DELETED);
        assertThat(shelter.isActive()).isFalse();
        verify(historyService).recordHistory(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteById_shouldReturnFalse_whenShelterDoesNotExist() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.deleteById(2L, 1L)).isFalse();
    }

    @Test
    void deleteById_shouldReturnFalse_whenShelterAlreadyDeleted() {
        when(repository.findById(2L)).thenReturn(Optional.of(shelter(ShelterStatus.DELETED, false)));

        assertThat(service.deleteById(2L, 1L)).isFalse();
    }

    @Test
    void deleteById_shouldThrow_whenShelterHasActivePets() {
        when(repository.findById(2L)).thenReturn(Optional.of(shelter(ShelterStatus.ACTIVE, true)));
        when(petServiceClient.getActivePetsByShelter(2L)).thenReturn(ResponseEntity.ok(List.of(
                new PetResponse(10L, "Cholito", "Perro", "Mestizo", 3, "MEDIUM", "Cafe", "AVAILABLE", "Tranquilo", 2L)
        )));

        assertThatThrownBy(() -> service.deleteById(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede eliminar el refugio porque tiene mascotas activas");
    }

    @Test
    void deleteById_shouldThrow_whenShelterHasActiveStaff() {
        when(repository.findById(2L)).thenReturn(Optional.of(shelter(ShelterStatus.ACTIVE, true)));
        when(petServiceClient.getActivePetsByShelter(2L)).thenReturn(ResponseEntity.ok(List.of()));
        when(staffServiceClient.getActiveStaffByShelter(2L)).thenReturn(ResponseEntity.ok(List.of(
                new StaffResponse(5L, 1L, 2L, "VETERINARIAN", "123", "staff@mail.com", "2026-01-01", "ACTIVE", null, null)
        )));

        assertThatThrownBy(() -> service.deleteById(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede eliminar el refugio porque tiene staff activo");
    }

    @Test
    void deleteById_shouldThrow_whenShelterHasActiveSupplies() {
        when(repository.findById(2L)).thenReturn(Optional.of(shelter(ShelterStatus.ACTIVE, true)));
        when(petServiceClient.getActivePetsByShelter(2L)).thenReturn(ResponseEntity.ok(List.of()));
        when(staffServiceClient.getActiveStaffByShelter(2L)).thenReturn(ResponseEntity.ok(List.of()));
        when(supplyServiceClient.getActiveSuppliesByShelter(2L)).thenReturn(ResponseEntity.ok(List.of(
                new SupplyResponse(7L, "Alimento", "Saco", 10, "kg", "FOOD", 2L, "Proveedor", 2, "AVAILABLE", null, null)
        )));

        assertThatThrownBy(() -> service.deleteById(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede eliminar el refugio porque tiene insumos activos");
    }
    private ShelterCommand command(ShelterStatus status) {
        return new ShelterCommand("Refugio Central", "refugio@mail.com", "123456789", "Rescate animal", status);
    }

    private Shelter shelter(ShelterStatus status, boolean active) {
        Shelter shelter = new Shelter();
        shelter.setId(2L);
        shelter.setName("Refugio Central");
        shelter.setEmail("refugio@mail.com");
        shelter.setPhone("123456789");
        shelter.setDescription("Rescate animal");
        shelter.setStatus(status);
        shelter.setActive(active);
        return shelter;
    }
}
