package com.adoptapp.followupservice.model;

import jakarta.persistence.*;
<<<<<<< HEAD

@Entity
@Table(name = "followups")
=======
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "followups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> origin/camila-dev
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD
    private String adopterName;

    private String petName;

    private String visitDate;

    private String comments;

    @Enumerated(EnumType.STRING)
    private FollowUpStatus status;

    public FollowUp() {
    }

    public FollowUp(Long id,
                    String adopterName,
                    String petName,
                    String visitDate,
                    String comments,
                    FollowUpStatus status) {

        this.id = id;
        this.adopterName = adopterName;
        this.petName = petName;
        this.visitDate = visitDate;
        this.comments = comments;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getAdopterName() {
        return adopterName;
    }

    public void setAdopterName(String adopterName) {
        this.adopterName = adopterName;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public FollowUpStatus getStatus() {
        return status;
    }

    public void setStatus(FollowUpStatus status) {
        this.status = status;
    }
}
=======
    @Column(nullable = false, length = 100)
    private String adopterName;

    @Column(nullable = false, length = 100)
    private String petName;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "pet_id")
    private Long petId;

    @Column(name = "adoption_id")
    private Long adoptionId;

    @Column(nullable = false)
    private LocalDateTime visitDate;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FollowUpStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
>>>>>>> origin/camila-dev
