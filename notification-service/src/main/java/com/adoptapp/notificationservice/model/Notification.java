package com.adoptapp.notificationservice.model;

import jakarta.persistence.*;
<<<<<<< HEAD

@Entity
@Table(name = "notifications")
=======
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> origin/camila-dev
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD
    private String recipient;

    private String message;

    private String type;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    public Notification() {
    }

    public Notification(Long id,
                        String recipient,
                        String message,
                        String type,
                        NotificationStatus status) {

        this.id = id;
        this.recipient = recipient;
        this.message = message;
        this.type = type;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
}
=======
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255)
    private String recipient;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

}
>>>>>>> origin/camila-dev
