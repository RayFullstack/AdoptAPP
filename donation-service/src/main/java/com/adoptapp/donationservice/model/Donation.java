package com.adoptapp.donationservice.model;

import jakarta.persistence.*;
<<<<<<< HEAD

@Entity
@Table(name = "donations")
=======
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> origin/camila-dev
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD
    private String donorName;

    private Double amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private DonationStatus status;

    public Donation() {
    }

    public Donation(Long id,
                    String donorName,
                    Double amount,
                    String description,
                    DonationStatus status) {

        this.id = id;
        this.donorName = donorName;
        this.amount = amount;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
=======
    @Column(nullable = false, length = 100)
    private String donorName;

    @Column(nullable = false, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "shelter_id", nullable = false)
    private Long shelterId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
>>>>>>> origin/camila-dev
    }
}