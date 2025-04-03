package com.qrave.qrservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_qr_codes")
public class UserQrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "qr_code", columnDefinition = "TEXT", nullable = false)
    private String qrCode;

    @Column(name = "last_regeneration")
    private LocalDateTime lastRegeneration;


    // Constructor vacío
    public UserQrCode() {}

    // Constructor completo
    public UserQrCode(Long userId, String qrCode) {
        this.userId = userId;
        this.qrCode = qrCode;
        this.lastRegeneration = lastRegeneration;
    }

    // Getters y Setters


    public LocalDateTime getLastRegeneration() {
        return lastRegeneration;
    }

    public void setLastRegeneration(LocalDateTime lastRegeneration) {
        this.lastRegeneration = lastRegeneration;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
