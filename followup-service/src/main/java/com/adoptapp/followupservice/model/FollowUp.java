package com.adoptapp.followupservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "followups")
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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