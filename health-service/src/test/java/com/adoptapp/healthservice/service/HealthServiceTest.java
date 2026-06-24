package com.adoptapp.healthservice.service;

import com.adoptapp.healthservice.client.NotificationServiceClient;
import com.adoptapp.healthservice.client.PetServiceClient;
import com.adoptapp.healthservice.client.StaffServiceClient;
import com.adoptapp.healthservice.client.UserServiceClient;
import com.adoptapp.healthservice.dto.HealthCommand;
import com.adoptapp.healthservice.dto.HealthResult;
import com.adoptapp.healthservice.dto.PetResponse;
import com.adoptapp.healthservice.dto.UserResponse;
import com.adoptapp.healthservice.model.Health;
import com.adoptapp.healthservice.model.HealthHistory;
import com.adoptapp.healthservice.model.HealthStatus;
import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import com.adoptapp.healthservice.repository.HealthHistoryRepository;
import com.adoptapp.healthservice.repository.HealthRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock private HealthRepository healthRepository;
    @Mock private HealthHistoryRepository healthHistoryRepository;
    @Mock private PetServiceClient petServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;
    @Mock private UserServiceClient userServiceClient;
    @Mock private StaffServiceClient staffServiceClient;

    @InjectMocks
    private HealthService service;

    @Test
    void create_shouldCreateActiveHealth_whenDataIsValid() {
        HealthCommand command = command();
        when(healthRepository.existsByPetIdAndStatus(10L, HealthStatus.ACTIVE)).thenReturn(false);
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet()));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "vet@mail.com")));
        when(healthRepository.save(any(Health.class))).thenAnswer(invocation -> {
            Health health = invocation.getArgument(0);
            health.setId(5L);
            health.setStatus(HealthStatus.ACTIVE);
            return health;
        });

        HealthResult result = service.create(command);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.petId()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(HealthStatus.ACTIVE);
        verify(healthHistoryRepository).save(any(HealthHistory.class));
    }

    @Test
    void create_shouldThrow_whenActiveHealthAlreadyExistsForPet() {
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet()));
        when(healthRepository.existsByPetIdAndStatus(10L, HealthStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.create(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya tiene una ficha");

        verify(healthRepository, never()).save(any(Health.class));
    }

    @Test
    void getById_shouldReturnEmpty_whenHealthIsDeleted() {
        Health health = health();
        health.setStatus(HealthStatus.DELETED);
        when(healthRepository.findById(5L)).thenReturn(Optional.of(health));

        assertThat(service.getById(5L)).isEmpty();
    }

    @Test
    void deleteByPetId_shouldMarkHealthDeleted_whenActiveHealthExists() {
        Health health = health();
        when(healthRepository.findByPetIdAndStatus(10L, HealthStatus.ACTIVE)).thenReturn(Optional.of(health));
        when(healthRepository.save(any(Health.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.deleteByPetId(10L);

        assertThat(result).isTrue();
        assertThat(health.getStatus()).isEqualTo(HealthStatus.DELETED);
        verify(healthHistoryRepository).save(any(HealthHistory.class));
    }


    @Test
    void updateById_shouldThrow_whenHealthIsDeleted() {
        Health health = health();
        health.setStatus(HealthStatus.DELETED);

        when(healthRepository.findById(5L)).thenReturn(Optional.of(health));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "vet@mail.com")));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet()));

        assertThatThrownBy(() -> service.updateById(5L, command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede actualizar una ficha clinica eliminada");

        verify(healthRepository, never()).save(any(Health.class));
    }

    @Test
    void updateById_shouldThrow_whenPetIdChanges() {
        Health health = health();
        HealthCommand command = new HealthCommand(1L, 99L, VaccinationStatus.VACCINATED,
                SterilizationStatus.STERILIZED, "Sin enfermedades");

        PetResponse otherPet = new PetResponse(99L, "Luna", "Gato", "Mestizo", 2, "SMALL", "Negro",
                "AVAILABLE", true, true, null, "Tranquila", 2L);

        when(healthRepository.findById(5L)).thenReturn(Optional.of(health));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "vet@mail.com")));
        when(petServiceClient.getPetById(99L)).thenReturn(ResponseEntity.ok(otherPet));

        assertThatThrownBy(() -> service.updateById(5L, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede cambiar la mascota asociada a una ficha clinica");

        verify(healthRepository, never()).save(any(Health.class));
    }
    @Test
    void create_shouldThrow_whenPetDoesNotExist() {
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La mascota con ID 10 no existe");
    }

    @Test
    void create_shouldThrow_whenUserDoesNotExist() {
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet()));
        when(healthRepository.existsByPetIdAndStatus(10L, HealthStatus.ACTIVE)).thenReturn(false);
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El usuario con ID 1 no existe");
    }

    @Test
    void getHealth_shouldReturnOnlyActiveHealthRecords() {
        when(healthRepository.findByStatus(HealthStatus.ACTIVE)).thenReturn(List.of(health()));

        List<HealthResult> result = service.getHealth();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(HealthStatus.ACTIVE);
    }

    @Test
    void getVax_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.getVax("wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado de vacunacion invalido: wrong");
    }

    @Test
    void getSter_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.getSter("wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado de esterilizacion invalido: wrong");
    }

    @Test
    void updateById_shouldUpdateHealth_whenDataIsValid() {
        Health health = health();
        HealthCommand command = new HealthCommand(1L, 10L, VaccinationStatus.NOT_VACCINATED,
                SterilizationStatus.NOT_STERILIZED, "Alergia");

        when(healthRepository.findById(5L)).thenReturn(Optional.of(health));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "vet@mail.com")));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet()));
        when(healthRepository.save(any(Health.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<HealthResult> result = service.updateById(5L, command);

        assertThat(result).isPresent();
        assertThat(result.get().vaccinationStatus()).isEqualTo(VaccinationStatus.NOT_VACCINATED);
        assertThat(result.get().sterilizationStatus()).isEqualTo(SterilizationStatus.NOT_STERILIZED);
        assertThat(result.get().diseases()).isEqualTo("Alergia");
        verify(healthHistoryRepository).save(any(HealthHistory.class));
    }

    @Test
    void updateById_shouldReturnEmpty_whenHealthDoesNotExist() {
        when(healthRepository.findById(5L)).thenReturn(Optional.empty());

        assertThat(service.updateById(5L, command())).isEmpty();
    }

    @Test
    void updateById_shouldThrow_whenUserIdChanges() {
        Health health = health();
        HealthCommand command = new HealthCommand(2L, 10L, VaccinationStatus.VACCINATED,
                SterilizationStatus.STERILIZED, "Sin enfermedades");

        when(healthRepository.findById(5L)).thenReturn(Optional.of(health));
        when(userServiceClient.getUserById(2L)).thenReturn(ResponseEntity.ok(new UserResponse(2L, "other@mail.com")));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet()));

        assertThatThrownBy(() -> service.updateById(5L, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede cambiar el usuario asociado a una ficha clinica");
    }

    @Test
    void deleteById_shouldMarkHealthDeleted_whenHealthExists() {
        Health health = health();
        when(healthRepository.findById(5L)).thenReturn(Optional.of(health));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "vet@mail.com")));
        when(healthRepository.save(any(Health.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.deleteById(5L);

        assertThat(result).isTrue();
        assertThat(health.getStatus()).isEqualTo(HealthStatus.DELETED);
        verify(healthHistoryRepository).save(any(HealthHistory.class));
    }

    @Test
    void deleteById_shouldReturnFalse_whenHealthDoesNotExist() {
        when(healthRepository.findById(5L)).thenReturn(Optional.empty());

        assertThat(service.deleteById(5L)).isFalse();
    }
    private HealthCommand command() {
        return new HealthCommand(1L, 10L, VaccinationStatus.VACCINATED,
                SterilizationStatus.STERILIZED, "Sin enfermedades");
    }

    private PetResponse pet() {
        return new PetResponse(10L, "Cholito", "Perro", "Mestizo", 3, "MEDIUM", "Cafe",
                "AVAILABLE", true, true, null, "Tranquilo", 2L);
    }

    private Health health() {
        Health health = new Health();
        health.setId(5L);
        health.setUserId(1L);
        health.setPetId(10L);
        health.setVaccinationStatus(VaccinationStatus.VACCINATED);
        health.setSterilizationStatus(SterilizationStatus.STERILIZED);
        health.setDiseases("Sin enfermedades");
        health.setStatus(HealthStatus.ACTIVE);
        return health;
    }
}
