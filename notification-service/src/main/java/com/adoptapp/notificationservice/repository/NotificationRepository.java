package com.adoptapp.notificationservice.repository;

import com.adoptapp.notificationservice.model.Notification;
import com.adoptapp.notificationservice.model.NotificationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByStatus(NotificationStatus status);
    List<Notification> findByStatusNot(NotificationStatus status);
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndStatusNot(Long userId, NotificationStatus status);
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    List<Notification> findByUserIdOrShelterId(Long userId, Long shelterId);
    List<Notification> findByUserIdAndStatusNotOrShelterIdAndStatusNot(
            Long userId,
            NotificationStatus userStatus,
            Long shelterId,
            NotificationStatus shelterStatus);
    List<Notification> findByStatusAndUserIdOrStatusAndShelterId(
            NotificationStatus userStatus,
            Long userId,
            NotificationStatus shelterStatus,
            Long shelterId);
}
