package com.adoptapp.notificationservice.service;

import com.adoptapp.notificationservice.dto.NotificationCommand;
import com.adoptapp.notificationservice.dto.NotificationResult;
import com.adoptapp.notificationservice.model.Notification;
import com.adoptapp.notificationservice.model.NotificationStatus;
<<<<<<< HEAD
import com.adoptapp.notificationservice.repository.NotificationRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

=======
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
>>>>>>> origin/camila-dev
@Service
public class NotificationService {

    private final NotificationRepository repository;
<<<<<<< HEAD

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<NotificationResult> getNotifications() {

=======
    private final NotificationTypeRepository typeRepository;

    public NotificationService(NotificationRepository repository,
                               NotificationTypeRepository typeRepository) {
        this.repository = repository;
        this.typeRepository = typeRepository;
    }

    public List<NotificationResult> getNotifications() {
        log.debug("Obteniendo todas las notificaciones");
>>>>>>> origin/camila-dev
        return repository.findAll()
                .stream()
                .map(this::toResult)
                .toList();
    }

    public List<NotificationResult> getNotifications(String status) {
<<<<<<< HEAD

        NotificationStatus notificationStatus =
                NotificationStatus.valueOf(status.toUpperCase());

        return repository.findByStatus(notificationStatus)
                .stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<NotificationResult> getById(Long id) {

=======
        log.debug("Obteniendo notificaciones con status={}", status);
        try {
            NotificationStatus notificationStatus =
                    NotificationStatus.valueOf(status.toUpperCase());

            return repository.findByStatus(notificationStatus)
                    .stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para notificación: '{}'", status);
            return List.of();
        }
    }

    public Optional<NotificationResult> getById(Long id) {
        log.debug("Obteniendo notificación por id={}", id);
>>>>>>> origin/camila-dev
        return repository.findById(id)
                .map(this::toResult);
    }

<<<<<<< HEAD
    public NotificationResult create(NotificationCommand command) {

        Notification notification = new Notification(
                null,
                command.recipient(),
                command.message(),
                command.type(),
                command.status()
        );

        Notification saved = repository.save(notification);
=======
    @Transactional
    public NotificationResult create(NotificationCommand command) {
        log.info("Creando notificación: userId={}, type={}", command.userId(), command.typeName());

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
        log.info("Notificación creada exitosamente: id={}", saved.getId());
>>>>>>> origin/camila-dev

        return toResult(saved);
    }

<<<<<<< HEAD
    public Optional<NotificationResult> updateById(
            Long id,
            NotificationCommand command) {
=======
    @Transactional
    public Optional<NotificationResult> updateById(
            Long id,
            NotificationCommand command) {
        log.info("Actualizando notificación: id={}", id);
>>>>>>> origin/camila-dev

        return repository.findById(id)
                .map(existing -> {

                    existing.setRecipient(command.recipient());
                    existing.setMessage(command.message());
<<<<<<< HEAD
                    existing.setType(command.type());
                    existing.setStatus(command.status());

                    Notification updated = repository.save(existing);
=======

                    if (command.typeName() != null) {
                        NotificationType type = typeRepository.findByName(command.typeName())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Tipo de notificación no encontrado: " + command.typeName()));
                        existing.setType(type);
                    }

                    existing.setStatus(command.status());

                    Notification updated = repository.save(existing);
                    log.info("Notificación actualizada exitosamente: id={}", id);
>>>>>>> origin/camila-dev

                    return toResult(updated);
                });
    }

<<<<<<< HEAD
    public boolean deleteById(Long id) {

        if (!repository.existsById(id)) {
=======
    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando notificación: id={}", id);

        if (!repository.existsById(id)) {
            log.warn("Notificación a eliminar no encontrada: id={}", id);
>>>>>>> origin/camila-dev
            return false;
        }

        repository.deleteById(id);
<<<<<<< HEAD
=======
        log.info("Notificación eliminada exitosamente: id={}", id);
>>>>>>> origin/camila-dev

        return true;
    }

    private NotificationResult toResult(Notification notification) {

        return new NotificationResult(
                notification.getId(),
<<<<<<< HEAD
                notification.getRecipient(),
                notification.getMessage(),
                notification.getType(),
                notification.getStatus()
        );
    }
}
=======
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
>>>>>>> origin/camila-dev
