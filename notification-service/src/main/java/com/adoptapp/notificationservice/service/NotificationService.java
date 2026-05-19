package com.adoptapp.notificationservice.service;

import com.adoptapp.notificationservice.dto.NotificationCommand;
import com.adoptapp.notificationservice.dto.NotificationResult;
import com.adoptapp.notificationservice.model.Notification;
import com.adoptapp.notificationservice.model.NotificationStatus;
import com.adoptapp.notificationservice.model.NotificationType;
import com.adoptapp.notificationservice.repository.NotificationRepository;
import com.adoptapp.notificationservice.repository.NotificationTypeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationTypeRepository typeRepository;

    public NotificationService(NotificationRepository repository,
                               NotificationTypeRepository typeRepository) {
        this.repository = repository;
        this.typeRepository = typeRepository;
    }

    public List<NotificationResult> getNotifications() {

        return repository.findAll()
                .stream()
                .map(this::toResult)
                .toList();
    }

    public List<NotificationResult> getNotifications(String status) {

        NotificationStatus notificationStatus =
                NotificationStatus.valueOf(status.toUpperCase());

        return repository.findByStatus(notificationStatus)
                .stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<NotificationResult> getById(Long id) {

        return repository.findById(id)
                .map(this::toResult);
    }

    @Transactional
    public NotificationResult create(NotificationCommand command) {

        NotificationType type = typeRepository.findByName(command.typeName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de notificación no encontrado: " + command.typeName()));

        Notification notification = new Notification();
        notification.setUserId(command.userId());
        notification.setRecipient(command.recipient());
        notification.setMessage(command.message());
        notification.setType(type);
        notification.setStatus(command.status());

        Notification saved = repository.save(notification);

        return toResult(saved);
    }

    @Transactional
    public Optional<NotificationResult> updateById(
            Long id,
            NotificationCommand command) {

        return repository.findById(id)
                .map(existing -> {

                    existing.setRecipient(command.recipient());
                    existing.setMessage(command.message());

                    if (command.typeName() != null) {
                        NotificationType type = typeRepository.findByName(command.typeName())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Tipo de notificación no encontrado: " + command.typeName()));
                        existing.setType(type);
                    }

                    existing.setStatus(command.status());

                    Notification updated = repository.save(existing);

                    return toResult(updated);
                });
    }

    @Transactional
    public boolean deleteById(Long id) {

        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);

        return true;
    }

    private NotificationResult toResult(Notification notification) {

        return new NotificationResult(
                notification.getId(),
                notification.getUserId(),
                notification.getRecipient(),
                notification.getMessage(),
                notification.getType().getId(),
                notification.getType().getName(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
