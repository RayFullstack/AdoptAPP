package com.adoptapp.staffservice.service;

import com.adoptapp.staffservice.client.NotificationServiceClient;
import com.adoptapp.staffservice.client.ShelterServiceClient;
import com.adoptapp.staffservice.client.UserServiceClient;
import com.adoptapp.staffservice.dto.ShelterResponse;
import com.adoptapp.staffservice.dto.StaffCommand;
import com.adoptapp.staffservice.dto.StaffResult;
import com.adoptapp.staffservice.dto.UserResponse;
import com.adoptapp.staffservice.model.Staff;
import com.adoptapp.staffservice.model.StaffHistory;
import com.adoptapp.staffservice.model.StaffPosition;
import com.adoptapp.staffservice.model.StaffStatus;
import com.adoptapp.staffservice.repository.StaffHistoryRepository;
import com.adoptapp.staffservice.repository.StaffRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock private StaffRepository repository;
    @Mock private StaffHistoryRepository historyRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;
    @Mock private ShelterServiceClient shelterServiceClient;

    @InjectMocks
    private StaffService service;

    @Test
    void create_shouldCreateActiveStaff_whenDataIsValid() {
        StaffCommand command = command(StaffStatus.ACTIVE);
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "staff@mail.com")));
        when(repository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff staff = invocation.getArgument(0);
            staff.setId(5L);
            return staff;
        });
        when(repository.findById(5L)).thenReturn(Optional.of(staff(StaffStatus.ACTIVE)));

        StaffResult result = service.create(command);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.shelterId()).isEqualTo(2L);
        assertThat(result.status()).isEqualTo(StaffStatus.ACTIVE);
        verify(historyRepository).save(any(StaffHistory.class));
    }

    @Test
    void create_shouldThrow_whenShelterDoesNotExist() {
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command(StaffStatus.ACTIVE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refugio con ID 2 no existe");
    }

    @Test
    void getAllStaff_shouldHideInactiveStaff() {
        when(repository.findByStatusNot(StaffStatus.INACTIVE)).thenReturn(List.of(staff(StaffStatus.ACTIVE)));

        List<StaffResult> result = service.getAllStaff();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(StaffStatus.ACTIVE);
    }

    @Test
    void deleteById_shouldMarkStaffInactive_whenStaffExists() {
        Staff staff = staff(StaffStatus.ACTIVE);
        when(repository.findById(5L)).thenReturn(Optional.of(staff));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "staff@mail.com")));
        when(repository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.deleteById(5L);

        assertThat(result).isTrue();
        assertThat(staff.getStatus()).isEqualTo(StaffStatus.INACTIVE);
        verify(historyRepository).save(any(StaffHistory.class));
    }


    @Test
    void create_shouldThrow_whenUserDoesNotExist() {
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command(StaffStatus.ACTIVE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El usuario con ID 1 no existe");
    }

    @Test
    void getAllStaff_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.getAllStaff("wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status invalido: wrong");
    }

    @Test
    void getById_shouldReturnEmpty_whenStaffIsInactive() {
        when(repository.findById(5L)).thenReturn(Optional.of(staff(StaffStatus.INACTIVE)));

        assertThat(service.getById(5L)).isEmpty();
    }

    @Test
    void getByIdForShelter_shouldReturnEmpty_whenStaffBelongsToAnotherShelter() {
        when(repository.findById(5L)).thenReturn(Optional.of(staff(StaffStatus.ACTIVE)));

        assertThat(service.getByIdForShelter(5L, 99L)).isEmpty();
    }

    @Test
    void getByUserId_shouldReturnActiveStaff() {
        when(repository.findByUserIdAndStatus(1L, StaffStatus.ACTIVE)).thenReturn(Optional.of(staff(StaffStatus.ACTIVE)));

        Optional<StaffResult> result = service.getByUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(1L);
    }

    @Test
    void updateById_shouldUpdateStaff_whenDataIsValid() {
        Staff staff = staff(StaffStatus.ACTIVE);
        StaffCommand command = new StaffCommand(1L, 2L, StaffPosition.ADMINISTRATOR, "987654321",
                "staff@mail.com", LocalDateTime.of(2026, 1, 2, 10, 0), StaffStatus.ACTIVE);

        when(repository.findById(5L)).thenReturn(Optional.of(staff));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "staff@mail.com")));
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));
        when(repository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<StaffResult> result = service.updateById(5L, command);

        assertThat(result).isPresent();
        assertThat(result.get().position()).isEqualTo(StaffPosition.ADMINISTRATOR);
        assertThat(result.get().phone()).isEqualTo("987654321");
        verify(historyRepository).save(any(StaffHistory.class));
    }

    @Test
    void updateById_shouldReturnEmpty_whenStaffDoesNotExist() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThat(service.updateById(5L, command(StaffStatus.ACTIVE))).isEmpty();
    }

    @Test
    void updateById_shouldThrow_whenStaffIsInactive() {
        when(repository.findById(5L)).thenReturn(Optional.of(staff(StaffStatus.INACTIVE)));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "staff@mail.com")));

        assertThatThrownBy(() -> service.updateById(5L, command(StaffStatus.ACTIVE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede actualizar un staff inactivo");
    }

    @Test
    void updateById_shouldThrow_whenCommandTriesToSetInactive() {
        when(repository.findById(5L)).thenReturn(Optional.of(staff(StaffStatus.ACTIVE)));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.ok(new UserResponse(1L, "staff@mail.com")));
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "refugio@mail.com")));

        assertThatThrownBy(() -> service.updateById(5L, command(StaffStatus.INACTIVE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede marcar staff como inactivo desde update; use delete");
    }

    @Test
    void deleteById_shouldReturnFalse_whenStaffDoesNotExist() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThat(service.deleteById(5L)).isFalse();
    }

    @Test
    void deleteById_shouldReturnFalse_whenStaffAlreadyInactive() {
        when(repository.findById(5L)).thenReturn(Optional.of(staff(StaffStatus.INACTIVE)));

        assertThat(service.deleteById(5L)).isFalse();
    }
    private StaffCommand command(StaffStatus status) {
        return new StaffCommand(1L, 2L, StaffPosition.VETERINARIAN, "123456789",
                "staff@mail.com", LocalDateTime.of(2026, 1, 1, 10, 0), status);
    }

    private Staff staff(StaffStatus status) {
        Staff staff = new Staff();
        staff.setId(5L);
        staff.setUserId(1L);
        staff.setShelterId(2L);
        staff.setPosition(StaffPosition.VETERINARIAN);
        staff.setPhone("123456789");
        staff.setEmail("staff@mail.com");
        staff.setHireDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        staff.setStatus(status);
        return staff;
    }
}
