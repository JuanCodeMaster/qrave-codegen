package com.qrave.qrservice.controller;

import com.qrave.qrservice.dto.*;
import com.qrave.qrservice.model.UserQrCode;
import com.qrave.qrservice.repository.UserQrCodeRepository;
import com.qrave.qrservice.service.QrCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.Base64;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final UserQrCodeRepository qrRepository;

    public QrCodeController(QrCodeService qrCodeService, UserQrCodeRepository qrRepository) {
        this.qrCodeService = qrCodeService;
        this.qrRepository = qrRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<QrCodeResponseDTO> generate(@RequestBody QrCodeRequestDTO request) {
        UserQrCode qr = qrCodeService.generateQrCode(request.getUserId());
        return ResponseEntity.ok(new QrCodeResponseDTO("QR generado exitosamente", qr.getQrCode()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<QrCodeWithPasswordStatusDTO> get(@PathVariable Long userId) {
        UserQrCode qr = qrCodeService.getQrCode(userId);
        boolean hasPassword = qr.getPaymentPassword() != null;
        return ResponseEntity.ok(
                new QrCodeWithPasswordStatusDTO("QR recuperado", qr.getQrCode(), hasPassword)
        );
    }

    @PostMapping("/regenerate")
    public ResponseEntity<QrCodeResponseDTO> regenerate(@RequestBody QrCodeRequestDTO request) {
        UserQrCode qr = qrCodeService.regenerateQrCode(request.getUserId());
        return ResponseEntity.ok(new QrCodeResponseDTO("QR regenerado exitosamente", qr.getQrCode()));
    }

    @PostMapping("/set-password")
    public ResponseEntity<String> setPaymentPassword(@RequestBody SetPasswordRequest request) {
        UserQrCode qr = qrRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR no encontrado"));

        if (qr.getPaymentPassword() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya hay una contraseña establecida");
        }

        qr.setPaymentPassword(hashPassword(request.getPassword()));
        qrRepository.save(qr);

        return ResponseEntity.ok("Contraseña de pago establecida correctamente");
    }

    @PostMapping("/validate-password")
    public ResponseEntity<String> validatePassword(@RequestBody ValidatePasswordRequest request) {
        UserQrCode qr = qrRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR no encontrado"));

        String hashed = hashPassword(request.getPassword());
        if (!hashed.equals(qr.getPaymentPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta");
        }

        return ResponseEntity.ok("Contraseña válida");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encoded);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }
}
