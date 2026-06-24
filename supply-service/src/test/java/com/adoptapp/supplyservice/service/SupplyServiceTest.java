package com.adoptapp.supplyservice.service;

import com.adoptapp.supplyservice.client.NotificationServiceClient;
import com.adoptapp.supplyservice.client.ShelterServiceClient;
import com.adoptapp.supplyservice.client.StaffServiceClient;
import com.adoptapp.supplyservice.client.UserServiceClient;
import com.adoptapp.supplyservice.dto.ShelterResponse;
import com.adoptapp.supplyservice.dto.SupplyCommand;
import com.adoptapp.supplyservice.dto.SupplyResult;
import com.adoptapp.supplyservice.dto.UserResponse;
import com.adoptapp.supplyservice.model.Supply;
import com.adoptapp.supplyservice.model.SupplyCategory;
import com.adoptapp.supplyservice.model.SupplyHistory;
import com.adoptapp.supplyservice.model.SupplyStatus;
import com.adoptapp.supplyservice.repository.SupplyHistoryRepository;
import com.adoptapp.supplyservice.repository.SupplyRepository;
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
class SupplyServiceTest {

    @Mock private SupplyRepository supplyRepository;
    @Mock private SupplyHistoryRepository supplyHistoryRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;
    @Mock private ShelterServiceClient shelterServiceClient;
    @Mock private StaffServiceClient staffServiceClient;

    @InjectMocks
    private SupplyService service;

    @Test
    void create_shouldCreateSupply_whenDataIsValid() {
        SupplyCommand command = command(SupplyStatus.AVAILABLE);
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(invocation -> {
            Supply supply = invocation.getArgument(0);
            supply.setId(7L);
            return supply;
        });
        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply(SupplyStatus.AVAILABLE)));

        SupplyResult result = service.create(command);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.name()).isEqualTo("Alimento");
        assertThat(result.status()).isEqualTo("AVAILABLE");
        verify(supplyHistoryRepository).save(any(SupplyHistory.class));
    }

    @Test
    void create_shouldThrow_whenShelterDoesNotExist() {
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command(SupplyStatus.AVAILABLE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refugio con ID 2 no existe");
    }

    @Test
    void getSupplies_shouldHideInactiveSupplies() {
        when(supplyRepository.findByStatusNot(SupplyStatus.INACTIVE)).thenReturn(List.of(supply(SupplyStatus.AVAILABLE)));

        List<SupplyResult> result = service.getSupplies();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("AVAILABLE");
    }

    @Test
    void delete_shouldMarkSupplyInactive_whenSupplyExists() {
        Supply supply = supply(SupplyStatus.AVAILABLE);
        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));

        boolean result = service.delete(7L);

        assertThat(result).isTrue();
        assertThat(supply.getStatus()).isEqualTo(SupplyStatus.INACTIVE);
        verify(supplyHistoryRepository).save(any(SupplyHistory.class));
    }


    @Test
    void create_shouldThrow_whenUserDoesNotExist() {
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command(SupplyStatus.AVAILABLE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El usuario con ID 1 no existe");
    }

    @Test
    void getSupplies_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.getSupplies("wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status invalido: wrong");
    }

    @Test
    void getById_shouldReturnEmpty_whenSupplyIsInactive() {
        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply(SupplyStatus.INACTIVE)));

        assertThat(service.getById(7L)).isEmpty();
    }

    @Test
    void getByIdForShelter_shouldReturnEmpty_whenSupplyBelongsToAnotherShelter() {
        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply(SupplyStatus.AVAILABLE)));

        assertThat(service.getByIdForShelter(7L, 99L)).isEmpty();
    }

    @Test
    void findByShelterId_shouldReturnOnlyActiveSuppliesForShelter() {
        when(supplyRepository.findByShelterIdAndStatusNot(2L, SupplyStatus.INACTIVE))
                .thenReturn(List.of(supply(SupplyStatus.AVAILABLE)));

        List<SupplyResult> result = service.findByShelterId(2L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().shelterId()).isEqualTo(2L);
    }

    @Test
    void findByShelterId_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.findByShelterId(2L, "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status invalido: wrong");
    }

    @Test
    void update_shouldUpdateSupply_whenDataIsValid() {
        Supply supply = supply(SupplyStatus.AVAILABLE);
        SupplyCommand command = new SupplyCommand("Medicamento", "Antiparasitario", 20, "unidad", "MEDICINE",
                2L, 1L, "Proveedor", 4, SupplyStatus.AVAILABLE);

        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply));
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<SupplyResult> result = service.update(7L, command);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Medicamento");
        assertThat(result.get().quantity()).isEqualTo(20);
        assertThat(result.get().category()).isEqualTo("MEDICINE");
        verify(supplyHistoryRepository).save(any(SupplyHistory.class));
    }

    @Test
    void update_shouldReturnEmpty_whenSupplyDoesNotExist() {
        when(supplyRepository.findById(7L)).thenReturn(Optional.empty());

        assertThat(service.update(7L, command(SupplyStatus.AVAILABLE))).isEmpty();
    }

    @Test
    void update_shouldThrow_whenSupplyIsInactive() {
        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply(SupplyStatus.INACTIVE)));
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));

        assertThatThrownBy(() -> service.update(7L, command(SupplyStatus.AVAILABLE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede actualizar un insumo inactivo");
    }

    @Test
    void update_shouldThrow_whenCommandTriesToSetInactive() {
        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply(SupplyStatus.AVAILABLE)));
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));

        assertThatThrownBy(() -> service.update(7L, command(SupplyStatus.INACTIVE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede marcar un insumo como inactivo desde update; use delete");
    }

    @Test
    void delete_shouldReturnFalse_whenSupplyDoesNotExist() {
        when(supplyRepository.findById(7L)).thenReturn(Optional.empty());

        assertThat(service.delete(7L)).isFalse();
    }

    @Test
    void delete_shouldReturnFalse_whenSupplyAlreadyInactive() {
        when(supplyRepository.findById(7L)).thenReturn(Optional.of(supply(SupplyStatus.INACTIVE)));

        assertThat(service.delete(7L)).isFalse();
    }
    private SupplyCommand command(SupplyStatus status) {
        return new SupplyCommand("Alimento", "Saco alimento", 10, "kg", "FOOD", 2L, 1L,
                "Proveedor", 2, status);
    }

    private Supply supply(SupplyStatus status) {
        Supply supply = new Supply();
        supply.setId(7L);
        supply.setName("Alimento");
        supply.setDescription("Saco alimento");
        supply.setQuantity(10);
        supply.setUnit("kg");
        supply.setCategory(SupplyCategory.FOOD);
        supply.setShelterId(2L);
        supply.setSupplierName("Proveedor");
        supply.setMinimumStock(2);
        supply.setStatus(status);
        return supply;
    }
}
