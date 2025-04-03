package com.qrave.qrservice.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.qrave.qrservice.model.UserQrCode;
import com.qrave.qrservice.repository.UserQrCodeRepository;
import com.qrave.qrservice.service.QrCodeService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class QrCodeServiceImpl implements QrCodeService {

    private final UserQrCodeRepository repository;

    public QrCodeServiceImpl(UserQrCodeRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserQrCode generateQrCode(Long userId) {
        Optional<UserQrCode> existing = repository.findByUserId(userId);
        if (existing.isPresent()) return existing.get();

        String base64Qr = generateQrBase64("qrave-user-" + userId + "-" + System.currentTimeMillis());

        UserQrCode qr = new UserQrCode();
        qr.setUserId(userId);
        qr.setQrCode(base64Qr);

        return repository.save(qr);
    }

    @Override
    public UserQrCode getQrCode(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Código QR no encontrado"));
    }

    @Override
    public UserQrCode regenerateQrCode(Long userId) {
        UserQrCode qr = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Código QR no encontrado"));

        LocalDateTime lastRegeneration = qr.getLastRegeneration();
        if (lastRegeneration != null && lastRegeneration.plusHours(24).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("El código QR solo puede regenerarse una vez cada 24 horas.");
        }

        String newBase64Qr = generateQrBase64("qrave-user-" + userId + "-" + System.currentTimeMillis());
        qr.setQrCode(newBase64Qr);
        qr.setLastRegeneration(LocalDateTime.now());
        return repository.save(qr);
    }


    private String generateQrBase64(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Error al generar el código QR", e);
        }
    }
}
