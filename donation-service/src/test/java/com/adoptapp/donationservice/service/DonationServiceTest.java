package com.adoptapp.donationservice.service;

import com.adoptapp.donationservice.client.NotificationServiceClient;
import com.adoptapp.donationservice.client.ShelterServiceClient;
import com.adoptapp.donationservice.client.UserServiceClient;
import com.adoptapp.donationservice.dto.*;
import com.adoptapp.donationservice.model.Donation;
import com.adoptapp.donationservice.model.DonationHistory;
import com.adoptapp.donationservice.model.DonationStatus;
import com.adoptapp.donationservice.repository.DonationHistoryRepository;
import com.adoptapp.donationservice.repository.DonationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock DonationRepository repository;
    @Mock DonationHistoryRepository historyRepository;
    @Mock UserServiceClient userServiceClient;
    @Mock NotificationServiceClient notificationServiceClient;
    @Mock ShelterServiceClient shelterServiceClient;
    @InjectMocks DonationService service;

    @Test
    void getDonations_shouldExcludeCancelled() {
        when(repository.findByStatusNot(DonationStatus.CANCELLED))
                .thenReturn(List.of(donation(1L, DonationStatus.PENDING)));

        List<DonationResult> result = service.getDonations();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(DonationStatus.PENDING);
    }

    @Test
    void getDonationsByStatus_shouldReturnMatches_andRejectInvalidStatus() {
        when(repository.findByStatus(DonationStatus.COMPLETED))
                .thenReturn(List.of(donation(1L, DonationStatus.COMPLETED)));

        assertThat(service.getDonations("completed")).hasSize(1);
        assertThatThrownBy(() -> service.getDonations("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status invalido: unknown");
    }

    @Test
    void getById_shouldHideCancelled_andReturnActive() {
        when(repository.findById(1L)).thenReturn(Optional.of(donation(1L, DonationStatus.PENDING)));
        when(repository.findById(2L)).thenReturn(Optional.of(donation(2L, DonationStatus.CANCELLED)));

        assertThat(service.getById(1L)).isPresent();
        assertThat(service.getById(2L)).isEmpty();
    }

    @Test
    void getHistory_shouldMapHistory() {
        Donation donation = donation(1L, DonationStatus.COMPLETED);
        DonationHistory history = new DonationHistory();
        history.setDonation(donation);
        history.setAction("UPDATED");
        history.setPreviousStatus("PENDING");
        history.setNewStatus("COMPLETED");
        history.setPreviousAmount(new BigDecimal("1000"));
        history.setNewAmount(new BigDecimal("2000"));
        history.setComment("Actualizada");
        history.setChangedByUserId(5L);
        history.setChangedAt(LocalDateTime.now());
        when(historyRepository.findByDonationIdOrderByChangedAtDesc(1L)).thenReturn(List.of(history));

        List<DonationHistoryResponse> result = service.getHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().action()).isEqualTo("UPDATED");
    }

    @Test
    void create_shouldSavePendingDonation_whenReferencesExist() {
        DonationCommand command = command(DonationStatus.COMPLETED);
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "shelter@mail.com")));
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));
        when(repository.save(any(Donation.class))).thenAnswer(invocation -> {
            Donation saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(repository.findById(10L)).thenAnswer(invocation -> Optional.of(donation(10L, DonationStatus.PENDING)));

        DonationResult result = service.create(command);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(DonationStatus.PENDING);
        verify(historyRepository).save(any(DonationHistory.class));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void create_shouldRejectMissingShelter_orMissingUser() {
        DonationCommand command = command(DonationStatus.PENDING);
        when(shelterServiceClient.getShelterById(2L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El refugio con ID 2 no existe");

        reset(shelterServiceClient);
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "shelter@mail.com")));
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario con ID 1 no existe");
        verify(repository, never()).save(any());
    }

    @Test
    void updateById_shouldUpdateDonation_whenValid() {
        Donation existing = donation(10L, DonationStatus.PENDING);
        DonationCommand command = new DonationCommand("Camila", new BigDecimal("2500"),
                "Alimento", DonationStatus.COMPLETED, 1L, 2L);
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "shelter@mail.com")));
        when(repository.save(existing)).thenReturn(existing);

        Optional<DonationResult> result = service.updateById(10L, command);

        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(result.get().amount()).isEqualByComparingTo("2500");
        verify(historyRepository).save(any(DonationHistory.class));
    }

    @Test
    void updateById_shouldReturnEmpty_andRejectCancelled() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.updateById(99L, command(DonationStatus.COMPLETED))).isEmpty();

        Donation cancelled = donation(10L, DonationStatus.CANCELLED);
        when(repository.findById(10L)).thenReturn(Optional.of(cancelled));
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));
        when(shelterServiceClient.getShelterById(2L))
                .thenReturn(ResponseEntity.ok(new ShelterResponse(2L, "shelter@mail.com")));

        assertThatThrownBy(() -> service.updateById(10L, command(DonationStatus.COMPLETED)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede actualizar una donacion cancelada");
    }

    @Test
    void deleteById_shouldSoftDelete_andRejectMissingOrCancelled() {
        Donation active = donation(10L, DonationStatus.PENDING);
        when(repository.findById(10L)).thenReturn(Optional.of(active));
        when(repository.save(active)).thenReturn(active);

        assertThat(service.deleteById(10L)).isTrue();
        assertThat(active.getStatus()).isEqualTo(DonationStatus.CANCELLED);
        verify(historyRepository).save(any(DonationHistory.class));

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.deleteById(99L)).isFalse();

        Donation cancelled = donation(20L, DonationStatus.CANCELLED);
        when(repository.findById(20L)).thenReturn(Optional.of(cancelled));
        assertThat(service.deleteById(20L)).isFalse();
    }

    private DonationCommand command(DonationStatus status) {
        return new DonationCommand("Camila", new BigDecimal("1000"), "Alimento", status, 1L, 2L);
    }

    private Donation donation(Long id, DonationStatus status) {
        Donation donation = new Donation();
        donation.setId(id);
        donation.setDonorName("Camila");
        donation.setAmount(new BigDecimal("1000"));
        donation.setDescription("Alimento");
        donation.setStatus(status);
        donation.setUserId(1L);
        donation.setShelterId(2L);
        return donation;
    }
}
