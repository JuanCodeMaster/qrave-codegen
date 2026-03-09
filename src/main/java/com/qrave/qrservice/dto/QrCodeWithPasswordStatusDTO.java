package com.qrave.qrservice.dto;

public class QrCodeWithPasswordStatusDTO {
    private String qrCode;
    private boolean hasPassword;
    private String message;

    public QrCodeWithPasswordStatusDTO(String message, String qrCode, boolean hasPassword) {
        this.message = message;
        this.qrCode = qrCode;
        this.hasPassword = hasPassword;
    }

    public String getQrCode() {
        return qrCode;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public String getMessage() {
        return message;
    }
}
