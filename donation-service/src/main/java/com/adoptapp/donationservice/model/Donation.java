package com.adoptapp.donationservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    }
}