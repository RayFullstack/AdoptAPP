package com.adoptapp.notificationservice.config;

import com.adoptapp.notificationservice.model.Notification;
import com.adoptapp.notificationservice.model.NotificationStatus;
import com.adoptapp.notificationservice.model.NotificationType;
import com.adoptapp.notificationservice.repository.NotificationRepository;
import com.adoptapp.notificationservice.repository.NotificationTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("h2")
public class DataInitializer implements CommandLineRunner {

    private final NotificationTypeRepository typeRepository;
    private final NotificationRepository notificationRepository;

    public DataInitializer(NotificationTypeRepository typeRepository,
                           NotificationRepository notificationRepository) {
        this.typeRepository = typeRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        if (typeRepository.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();

        NotificationType created = typeRepository.save(new NotificationType(
                null, "ADOPTION_CREATED",
                "Se ha creado la adopción de la mascota {petId} por el usuario {userName}", "EMAIL"));

        NotificationType updated = typeRepository.save(new NotificationType(
                null, "ADOPTION_UPDATED",
                "La adopción {adoptionId} ha sido actualizada a estado {status}", "EMAIL"));

        NotificationType deleted = typeRepository.save(new NotificationType(
                null, "ADOPTION_DELETED",
                "La adopción {adoptionId} de la mascota {petId} ha sido eliminada", "EMAIL"));

        NotificationType petCreated = typeRepository.save(new NotificationType(
                null, "PET_CREATED", "La mascota {name} ha sido registrada", "PUSH"));

        NotificationType petUpdated = typeRepository.save(new NotificationType(
                null, "PET_UPDATED", "Los datos de {name} han sido actualizados", "PUSH"));

        NotificationType petDeleted = typeRepository.save(new NotificationType(
                null, "PET_DELETED", "La mascota {name} ha sido eliminada", "PUSH"));

        NotificationType petStatusChanged = typeRepository.save(new NotificationType(
                null, "PET_STATUS_CHANGED", "La mascota {name} cambió a estado {status}", "PUSH"));

        createNotification(1L, "lsimpson@mail.com",
                "Se ha creado la adopción de la mascota 1 por el usuario Lisa", created, now);
        createNotification(2L, "homerosimp@mail.com",
                "La adopción 1 ha sido actualizada a estado APPROVED", updated, now);
        createNotification(3L, "fakemail123@mail.com",
                "Notificación de prueba de PET_CREATED", petCreated, now);
    }

    private void createNotification(Long userId, String recipient, String message,
                                    NotificationType type, LocalDateTime now) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setRecipient(recipient);
        n.setMessage(message);
        n.setType(type);
        n.setStatus(NotificationStatus.SENT);
        n.setCreatedAt(now);
        notificationRepository.save(n);
    }
}
