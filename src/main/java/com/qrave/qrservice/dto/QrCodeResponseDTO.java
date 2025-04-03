// QrCodeResponseDTO.java
package com.qrave.qrservice.dto;

public class QrCodeResponseDTO {
    private String message;
    private String qrCode;

    public QrCodeResponseDTO(String message, String qrCode) {
        this.message = message;
        this.qrCode = qrCode;
    }

    public String getMessage() {
        return message;
    }

    public String getQrCode() {
        return qrCode;
    }
}
