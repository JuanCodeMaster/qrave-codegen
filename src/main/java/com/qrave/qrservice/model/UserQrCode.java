package com.qrave.qrservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_qr_codes")
public class UserQrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    private LocalDateTime lastRegeneration;

    @Column(name = "payment_password")
    private String paymentPassword; // ✅ Campo para la contraseña de pago encriptada

    // Getters y setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getQrCode() { return qrCode; }

    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public LocalDateTime getLastRegeneration() { return lastRegeneration; }

    public void setLastRegeneration(LocalDateTime lastRegeneration) {
        this.lastRegeneration = lastRegeneration;
    }

    public String getPaymentPassword() {
        return paymentPassword;
    }

    public void setPaymentPassword(String paymentPassword) {
        this.paymentPassword = paymentPassword;
    }
}
