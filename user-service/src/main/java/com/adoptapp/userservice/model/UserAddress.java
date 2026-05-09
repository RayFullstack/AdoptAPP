package com.adoptapp.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String street;

    @Column(nullable = false, length = 100)
    private String homeNumber;

    @Column(nullable = false, length = 100)
    private String postalCode;

    @Column(nullable = false)
    private Boolean primaryAddress;

    @Column(nullable = false, length = 100)
    private String type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
