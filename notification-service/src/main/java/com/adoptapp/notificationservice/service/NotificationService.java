package com.adoptapp.notificationservice.service;

import com.adoptapp.notificationservice.dto.NotificationCommand;
import com.adoptapp.notificationservice.dto.NotificationResult;
import com.adoptapp.notificationservice.model.Notification;
import com.adoptapp.notificationservice.model.NotificationStatus;
import com.adoptapp.notificationservice.repository.NotificationRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
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

    public NotificationResult create(NotificationCommand command) {

        Notification notification = new Notification(
                null,
                command.recipient(),
                command.message(),
                command.type(),
                command.status()
        );

        Notification saved = repository.save(notification);

        return toResult(saved);
    }

    public Optional<NotificationResult> updateById(
            Long id,
            NotificationCommand command) {

        return repository.findById(id)
                .map(existing -> {

                    existing.setRecipient(command.recipient());
                    existing.setMessage(command.message());
                    existing.setType(command.type());
                    existing.setStatus(command.status());

                    Notification updated = repository.save(existing);

                    return toResult(updated);
                });
    }

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
                notification.getRecipient(),
                notification.getMessage(),
                notification.getType(),
                notification.getStatus()
        );
    }
}