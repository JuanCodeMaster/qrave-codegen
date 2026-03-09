package com.qrave.qrservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class InvalidatedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String token;

    private LocalDateTime expiration;

    public InvalidatedToken() {}

    public InvalidatedToken(String token, LocalDateTime expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    // Getters y setters
}
