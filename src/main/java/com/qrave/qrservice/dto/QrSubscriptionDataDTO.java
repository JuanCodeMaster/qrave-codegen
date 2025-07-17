package com.qrave.qrservice.dto;

public class QrSubscriptionDataDTO {
    private String phoneNumber;
    private String code;
    private String subscriptionToken;
    private String fullName;
    private Long userId;
    // Getters y Setters
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getSubscriptionToken() { return subscriptionToken; }
    public void setSubscriptionToken(String subscriptionToken) { this.subscriptionToken = subscriptionToken; }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}

