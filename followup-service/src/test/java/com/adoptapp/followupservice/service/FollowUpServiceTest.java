package com.adoptapp.followupservice.service;

import com.adoptapp.followupservice.client.NotificationServiceClient;
import com.adoptapp.followupservice.client.PetServiceClient;
import com.adoptapp.followupservice.client.UserServiceClient;
import com.adoptapp.followupservice.dto.*;
import com.adoptapp.followupservice.model.FollowUp;
import com.adoptapp.followupservice.model.FollowUpHistory;
import com.adoptapp.followupservice.model.FollowUpStatus;
import com.adoptapp.followupservice.repository.FollowUpHistoryRepository;
import com.adoptapp.followupservice.repository.FollowUpRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUpServiceTest {

    @Mock FollowUpRepository repository;
    @Mock FollowUpHistoryRepository historyRepository;
    @Mock UserServiceClient userServiceClient;
    @Mock NotificationServiceClient notificationServiceClient;
    @Mock PetServiceClient petServiceClient;
    @InjectMocks FollowUpService service;

    @Test
    void listAndFind_shouldExcludeCancelled_andValidateStatus() {
        when(repository.findByStatusNot(FollowUpStatus.CANCELLED))
                .thenReturn(List.of(followUp(1L, FollowUpStatus.PENDING)));
        when(repository.findByStatus(FollowUpStatus.COMPLETED))
                .thenReturn(List.of(followUp(2L, FollowUpStatus.COMPLETED)));
        when(repository.findById(1L)).thenReturn(Optional.of(followUp(1L, FollowUpStatus.PENDING)));
        when(repository.findById(3L)).thenReturn(Optional.of(followUp(3L, FollowUpStatus.CANCELLED)));

        assertThat(service.getFollowUps()).hasSize(1);
        assertThat(service.getFollowUps("completed")).hasSize(1);
        assertThat(service.getById(1L)).isPresent();
        assertThat(service.getById(3L)).isEmpty();
        assertThatThrownBy(() -> service.getFollowUps("bad"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status invalido: bad");
    }

    @Test
    void getHistory_shouldMapHistory() {
        FollowUp entity = followUp(1L, FollowUpStatus.COMPLETED);
        FollowUpHistory history = new FollowUpHistory();
        history.setFollowUp(entity);
        history.setAction("UPDATED");
        history.setPreviousStatus("PENDING");
        history.setNewStatus("COMPLETED");
        history.setComment("Visita realizada");
        history.setChangedByUserId(1L);
        history.setChangedAt(LocalDateTime.now());
        when(historyRepository.findByFollowUpIdOrderByChangedAtDesc(1L)).thenReturn(List.of(history));

        assertThat(service.getHistory(1L)).hasSize(1);
        assertThat(service.getHistory(1L).getFirst().action()).isEqualTo("UPDATED");
    }

    @Test
    void create_shouldSavePendingFollowUp_whenReferencesExist() {
        FollowUpCommand command = command(FollowUpStatus.PENDING, 1L, 10L, 100L);
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.ok(pet()));
        when(repository.save(any(FollowUp.class))).thenAnswer(invocation -> {
            FollowUp saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });
        when(repository.findById(5L)).thenReturn(Optional.of(followUp(5L, FollowUpStatus.PENDING)));

        FollowUpResult result = service.create(command);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.status()).isEqualTo(FollowUpStatus.PENDING);
        verify(historyRepository).save(any(FollowUpHistory.class));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void create_shouldRejectMissingUser_orMissingPet() {
        FollowUpCommand command = command(FollowUpStatus.PENDING, 1L, 10L, 100L);
        when(userServiceClient.getUserById(1L)).thenReturn(ResponseEntity.notFound().build());
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario con ID 1 no existe");

        reset(userServiceClient);
        when(userServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(new UserResponse(1L, "user@mail.com")));
        when(petServiceClient.getPetById(10L)).thenReturn(ResponseEntity.notFound().build());
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mascota con ID 10 no existe");
        verify(repository, never()).save(any());
    }

    @Test
    void updateById_shouldUpdateStatus_whenIdentityDoesNotChange() {
        FollowUp existing = followUp(1L, FollowUpStatus.PENDING);
        FollowUpCommand command = command(FollowUpStatus.COMPLETED, 1L, 10L, 100L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Optional<FollowUpResult> result = service.updateById(1L, command);

        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(FollowUpStatus.COMPLETED);
        verify(historyRepository).save(any(FollowUpHistory.class));
    }

    @Test
    void updateById_shouldReturnEmpty_andRejectCancelledOrIdentityChanges() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.updateById(99L, command(FollowUpStatus.COMPLETED, 1L, 10L, 100L))).isEmpty();

        FollowUp cancelled = followUp(1L, FollowUpStatus.CANCELLED);
        when(repository.findById(1L)).thenReturn(Optional.of(cancelled));
        assertThatThrownBy(() -> service.updateById(1L, command(FollowUpStatus.COMPLETED, 1L, 10L, 100L)))
                .hasMessage("No se puede actualizar un seguimiento cancelado");

        FollowUp active = followUp(2L, FollowUpStatus.PENDING);
        when(repository.findById(2L)).thenReturn(Optional.of(active));
        assertThatThrownBy(() -> service.updateById(2L, command(FollowUpStatus.COMPLETED, 9L, 10L, 100L)))
                .hasMessage("No se puede cambiar el usuario asociado al seguimiento");
        assertThatThrownBy(() -> service.updateById(2L, command(FollowUpStatus.COMPLETED, 1L, 99L, 100L)))
                .hasMessage("No se puede cambiar la mascota asociada al seguimiento");
        assertThatThrownBy(() -> service.updateById(2L, command(FollowUpStatus.COMPLETED, 1L, 10L, 999L)))
                .hasMessage("No se puede cambiar la adopcion asociada al seguimiento");
    }

    @Test
    void deleteById_shouldSoftDelete_andRejectMissingOrCancelled() {
        FollowUp active = followUp(1L, FollowUpStatus.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(active));
        when(repository.save(active)).thenReturn(active);
        assertThat(service.deleteById(1L)).isTrue();
        assertThat(active.getStatus()).isEqualTo(FollowUpStatus.CANCELLED);

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.deleteById(99L)).isFalse();

        FollowUp cancelled = followUp(2L, FollowUpStatus.CANCELLED);
        when(repository.findById(2L)).thenReturn(Optional.of(cancelled));
        assertThat(service.deleteById(2L)).isFalse();
    }

    private FollowUpCommand command(FollowUpStatus status, Long userId, Long petId, Long adoptionId) {
        return new FollowUpCommand("Camila", "Benito", userId, petId, adoptionId,
                LocalDateTime.now().plusDays(7), "Todo bien", status);
    }

    private FollowUp followUp(Long id, FollowUpStatus status) {
        FollowUp followUp = new FollowUp();
        followUp.setId(id);
        followUp.setAdopterName("Camila");
        followUp.setPetName("Benito");
        followUp.setUserId(1L);
        followUp.setPetId(10L);
        followUp.setAdoptionId(100L);
        followUp.setVisitDate(LocalDateTime.now().plusDays(7));
        followUp.setComments("Todo bien");
        followUp.setStatus(status);
        return followUp;
    }

    private PetResponse pet() {
        return new PetResponse(10L, "Benito", "Perro", "Samoyedo", 3, "MEDIUM", "Blanco",
                "AVAILABLE", true, true, null, "Amoroso", 2L);
    }
}
