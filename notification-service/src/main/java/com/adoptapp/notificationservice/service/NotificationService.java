package com.adoptapp.notificationservice.service;

import com.adoptapp.notificationservice.client.StaffServiceClient;
import com.adoptapp.notificationservice.client.UserServiceClient;
import com.adoptapp.notificationservice.dto.NotificationCommand;
import com.adoptapp.notificationservice.dto.NotificationResult;
import com.adoptapp.notificationservice.dto.StaffResponse;
import com.adoptapp.notificationservice.model.Notification;
import com.adoptapp.notificationservice.model.NotificationStatus;
import com.adoptapp.notificationservice.model.NotificationType;
import com.adoptapp.notificationservice.repository.NotificationRepository;
import com.adoptapp.notificationservice.repository.NotificationTypeRepository;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationTypeRepository typeRepository;
    private final UserServiceClient userServiceClient;
    private final StaffServiceClient staffServiceClient;

    public NotificationService(NotificationRepository repository,
                               NotificationTypeRepository typeRepository,
                               UserServiceClient userServiceClient,
                               StaffServiceClient staffServiceClient) {
        this.repository = repository;
        this.typeRepository = typeRepository;
        this.userServiceClient = userServiceClient;
        this.staffServiceClient = staffServiceClient;
    }

    public List<NotificationResult> getNotifications() {
        log.debug("Obteniendo todas las notificaciones");
        return repository.findByStatusNot(NotificationStatus.ARCHIVED)
                .stream()
                .map(this::toResult)
                .toList();
    }

    public List<NotificationResult> getNotifications(String status) {
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
            throw new IllegalArgumentException("Status invalido: " + status);
        }
    }

    public Optional<NotificationResult> getById(Long id) {
        log.debug("Obteniendo notificación por id={}", id);
        return repository.findById(id)
                .filter(notification -> notification.getStatus() != NotificationStatus.ARCHIVED)
                .map(this::toResult);
    }

    public Optional<NotificationResult> getByIdIncludingArchived(Long id) {
        log.debug("Obteniendo notificaciÃ³n por id={}, incluyendo archivadas", id);
        return repository.findById(id)
                .map(this::toResult);
    }

    public List<NotificationResult> getNotificationsByUser(Long userId) {
        return repository.findByUserIdAndStatusNot(userId, NotificationStatus.ARCHIVED).stream()
                .map(this::toResult)
                .toList();
    }

    public List<NotificationResult> getNotificationsByUser(Long userId, String status) {
        try {
            NotificationStatus notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
            if (notificationStatus == NotificationStatus.ARCHIVED) {
                return List.of();
            }
            return repository.findByUserIdAndStatus(userId, notificationStatus).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para notificación: '{}'", status);
            throw new IllegalArgumentException("Status invalido: " + status);
        }
    }

    public List<NotificationResult> getNotificationsByUserOrShelter(Long userId, Long shelterId) {
        return repository.findByUserIdAndStatusNotOrShelterIdAndStatusNot(
                        userId,
                        NotificationStatus.ARCHIVED,
                        shelterId,
                        NotificationStatus.ARCHIVED)
                .stream()
                .map(this::toResult)
                .toList();
    }

    public List<NotificationResult> getNotificationsByUserOrShelter(Long userId, Long shelterId, String status) {
        try {
            NotificationStatus notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
            if (notificationStatus == NotificationStatus.ARCHIVED) {
                return List.of();
            }
            return repository.findByStatusAndUserIdOrStatusAndShelterId(
                            notificationStatus,
                            userId,
                            notificationStatus,
                            shelterId)
                    .stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado invalido para notificacion: '{}'", status);
            throw new IllegalArgumentException("Status invalido: " + status);
        }
    }

    public Long getUserIdByEmail(String email) {
        ResponseEntity<UserAuthResponse> response = userServiceClient.getUserAuthByEmail(email);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("Usuario autenticado no encontrado: " + email);
        }

        return response.getBody().id();
    }

    public Long getShelterIdForStaffUser(Long userId) {
        ResponseEntity<StaffResponse> response = staffServiceClient.getStaffByUserId(userId);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("El usuario no tiene staff activo asociado");
        }

        return response.getBody().shelterId();
    }

    public Optional<NotificationResult> getByIdForUser(Long id, Long userId) {
        return repository.findById(id)
                .filter(notification -> notification.getStatus() != NotificationStatus.ARCHIVED)
                .filter(notification -> userId.equals(notification.getUserId()))
                .map(this::toResult);
    }

    public Optional<NotificationResult> getByIdForUserOrShelter(Long id, Long userId, Long shelterId) {
        return repository.findById(id)
                .filter(notification -> notification.getStatus() != NotificationStatus.ARCHIVED)
                .filter(notification -> userId.equals(notification.getUserId())
                        || shelterId.equals(notification.getShelterId()))
                .map(this::toResult);
    }

    @Transactional
    public NotificationResult create(NotificationCommand command) {
        log.info("Creando notificación: userId={}, type={}", command.userId(), command.typeName());

        NotificationType type = typeRepository.findByName(command.typeName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de notificación no encontrado: " + command.typeName()));

        Notification notification = new Notification();
        notification.setUserId(command.userId());
        notification.setShelterId(command.shelterId());
        notification.setRecipient(command.recipient());
        notification.setMessage(command.message());
        notification.setType(type);
        notification.setStatus(command.status());

        Notification saved = repository.save(notification);
        log.info("Notificación creada exitosamente: id={}", saved.getId());

        return toResult(saved);
    }

    @Transactional
    public Optional<NotificationResult> updateById(
            Long id,
            NotificationCommand command) {
        log.info("Actualizando notificación: id={}", id);

        return repository.findById(id)
                .map(existing -> {
                    if (existing.getStatus() == NotificationStatus.ARCHIVED) {
                        throw new IllegalArgumentException("No se puede actualizar una notificacion archivada");
                    }

                    existing.setRecipient(command.recipient());
                    existing.setMessage(command.message());
                    existing.setUserId(command.userId());
                    existing.setShelterId(command.shelterId());

                    if (command.typeName() != null) {
                        NotificationType type = typeRepository.findByName(command.typeName())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Tipo de notificación no encontrado: " + command.typeName()));
                        existing.setType(type);
                    }

                    existing.setStatus(command.status());

                    Notification updated = repository.save(existing);
                    log.info("Notificación actualizada exitosamente: id={}", id);

                    return toResult(updated);
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando notificación: id={}", id);

        Optional<Notification> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Notificación a eliminar no encontrada: id={}", id);
            return false;
        }

        Notification notification = found.get();
        if (notification.getStatus() == NotificationStatus.ARCHIVED) {
            log.warn("Notificacion ya archivada: id={}", id);
            return false;
        }

        notification.setStatus(NotificationStatus.ARCHIVED);
        repository.save(notification);
        log.info("Notificación eliminada exitosamente: id={}", id);

        return true;
    }

    private NotificationResult toResult(Notification notification) {

        return new NotificationResult(
                notification.getId(),
                notification.getUserId(),
                notification.getShelterId(),
                notification.getRecipient(),
                notification.getMessage(),
                notification.getType().getId(),
                notification.getType().getName(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }

}
