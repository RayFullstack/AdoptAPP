package com.adoptapp.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "phone_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPhone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String number;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
