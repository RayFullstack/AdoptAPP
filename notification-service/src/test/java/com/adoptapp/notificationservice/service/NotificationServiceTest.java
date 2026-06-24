package com.adoptapp.notificationservice.service;

import com.adoptapp.notificationservice.client.StaffServiceClient;
import com.adoptapp.notificationservice.client.UserServiceClient;
import com.adoptapp.notificationservice.dto.NotificationCommand;
import com.adoptapp.notificationservice.dto.NotificationResult;
import com.adoptapp.notificationservice.model.Notification;
import com.adoptapp.notificationservice.model.NotificationStatus;
import com.adoptapp.notificationservice.model.NotificationType;
import com.adoptapp.notificationservice.repository.NotificationRepository;
import com.adoptapp.notificationservice.repository.NotificationTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository repository;
    @Mock private NotificationTypeRepository typeRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private StaffServiceClient staffServiceClient;

    @InjectMocks
    private NotificationService service;

    @Test
    void create_shouldCreateNotification_whenTypeExists() {
        NotificationType type = type();
        NotificationCommand command = command(NotificationStatus.SENT);
        when(typeRepository.findByName("PET_CREATED")).thenReturn(Optional.of(type));
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return notification;
        });

        NotificationResult result = service.create(command);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.shelterId()).isEqualTo(2L);
        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.typeName()).isEqualTo("PET_CREATED");
        verify(repository).save(any(Notification.class));
    }

    @Test
    void create_shouldThrow_whenTypeDoesNotExist() {
        when(typeRepository.findByName("PET_CREATED")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(command(NotificationStatus.SENT)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de notificación no encontrado");
    }

    @Test
    void getById_shouldReturnEmpty_whenNotificationIsArchived() {
        Notification notification = notification(NotificationStatus.ARCHIVED);
        when(repository.findById(1L)).thenReturn(Optional.of(notification));

        assertThat(service.getById(1L)).isEmpty();
    }

    @Test
    void deleteById_shouldArchiveNotification_whenNotificationExists() {
        Notification notification = notification(NotificationStatus.SENT);
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.deleteById(1L);

        assertThat(result).isTrue();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.ARCHIVED);
        verify(repository).save(notification);
    }

    @Test
    void getNotifications_shouldHideArchivedNotifications() {
        Notification notification = notification(NotificationStatus.SENT);
        when(repository.findByStatusNot(NotificationStatus.ARCHIVED)).thenReturn(List.of(notification));

        List<NotificationResult> result = service.getNotifications();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(NotificationStatus.SENT);
    }


    @Test
    void getNotifications_shouldFilterByStatus_whenStatusIsValid() {
        Notification notification = notification(NotificationStatus.SENT);
        when(repository.findByStatus(NotificationStatus.SENT)).thenReturn(List.of(notification));

        List<NotificationResult> result = service.getNotifications("SENT");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void getNotifications_shouldThrow_whenStatusIsInvalid() {
        assertThatThrownBy(() -> service.getNotifications("wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status invalido: wrong");
    }

    @Test
    void getByIdIncludingArchived_shouldReturnArchivedNotification() {
        Notification notification = notification(NotificationStatus.ARCHIVED);
        when(repository.findById(1L)).thenReturn(Optional.of(notification));

        Optional<NotificationResult> result = service.getByIdIncludingArchived(1L);

        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(NotificationStatus.ARCHIVED);
    }

    @Test
    void getNotificationsByUser_shouldHideArchivedNotifications() {
        Notification notification = notification(NotificationStatus.SENT);
        when(repository.findByUserIdAndStatusNot(10L, NotificationStatus.ARCHIVED)).thenReturn(List.of(notification));

        List<NotificationResult> result = service.getNotificationsByUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().userId()).isEqualTo(10L);
    }

    @Test
    void getNotificationsByUser_shouldReturnEmpty_whenStatusIsArchived() {
        List<NotificationResult> result = service.getNotificationsByUser(10L, "ARCHIVED");

        assertThat(result).isEmpty();
        verify(repository, never()).findByUserIdAndStatus(any(), any());
    }

    @Test
    void getByIdForUserOrShelter_shouldReturnNotification_whenUserOwnsIt() {
        Notification notification = notification(NotificationStatus.SENT);
        when(repository.findById(1L)).thenReturn(Optional.of(notification));

        Optional<NotificationResult> result = service.getByIdForUserOrShelter(1L, 10L, 99L);

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(10L);
    }

    @Test
    void updateById_shouldUpdateNotification_whenDataIsValid() {
        Notification notification = notification(NotificationStatus.SENT);
        NotificationCommand command = new NotificationCommand(11L, 3L, "nuevo@mail.com",
                "Mensaje actualizado", "PET_CREATED", NotificationStatus.FAILED);

        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(typeRepository.findByName("PET_CREATED")).thenReturn(Optional.of(type()));
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<NotificationResult> result = service.updateById(1L, command);

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(11L);
        assertThat(result.get().shelterId()).isEqualTo(3L);
        assertThat(result.get().status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.get().message()).isEqualTo("Mensaje actualizado");
    }

    @Test
    void updateById_shouldThrow_whenNotificationIsArchived() {
        when(repository.findById(1L)).thenReturn(Optional.of(notification(NotificationStatus.ARCHIVED)));

        assertThatThrownBy(() -> service.updateById(1L, command(NotificationStatus.SENT)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede actualizar una notificacion archivada");
    }

    @Test
    void deleteById_shouldReturnFalse_whenNotificationDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThat(service.deleteById(1L)).isFalse();
    }
    private NotificationCommand command(NotificationStatus status) {
        return new NotificationCommand(10L, 2L, "refugio@mail.com", "Mascota creada", "PET_CREATED", status);
    }

    private Notification notification(NotificationStatus status) {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(10L);
        notification.setShelterId(2L);
        notification.setRecipient("refugio@mail.com");
        notification.setMessage("Mascota creada");
        notification.setType(type());
        notification.setStatus(status);
        return notification;
    }

    private NotificationType type() {
        NotificationType type = new NotificationType();
        type.setId(3L);
        type.setName("PET_CREATED");
        type.setTemplate("template");
        type.setChannel("EMAIL");
        return type;
    }
}
