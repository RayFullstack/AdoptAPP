package com.adoptapp.notificationservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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