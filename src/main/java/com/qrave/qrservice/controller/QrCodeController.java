package com.qrave.qrservice.controller;

import com.qrave.qrservice.dto.QrCodeRequestDTO;
import com.qrave.qrservice.dto.QrCodeResponseDTO;
import com.qrave.qrservice.dto.QrCodeWithPasswordStatusDTO;
import com.qrave.qrservice.dto.SetPasswordRequest;
import com.qrave.qrservice.dto.ValidatePasswordRequest;
import com.qrave.qrservice.model.UserQrCode;
import com.qrave.qrservice.repository.UserQrCodeRepository;
import com.qrave.qrservice.service.QrCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

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
    public ResponseEntity<?> generate(@RequestBody QrCodeRequestDTO request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "El userId es obligatorio.",
                        "status", HttpStatus.BAD_REQUEST.value()
                ));
            }

            UserQrCode qr = qrCodeService.generateQrCode(request.getUserId());
            return ResponseEntity.ok(
                    new QrCodeResponseDTO("QR generado exitosamente", qr.getQrCode())
            );
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "message", e.getReason(),
                    "status", e.getStatusCode().value()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "status", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> get(@PathVariable Long userId) {
        try {
            UserQrCode qr = qrCodeService.getQrCode(userId);
            boolean hasPassword = qr.getPaymentPassword() != null;

            return ResponseEntity.ok(
                    new QrCodeWithPasswordStatusDTO("QR recuperado", qr.getQrCode(), hasPassword)
            );
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "message", e.getReason(),
                    "status", e.getStatusCode().value()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", e.getMessage(),
                    "status", HttpStatus.NOT_FOUND.value()
            ));
        }
    }

    @PostMapping("/regenerate")
    public ResponseEntity<?> regenerate(@RequestBody QrCodeRequestDTO request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "El userId es obligatorio.",
                        "status", HttpStatus.BAD_REQUEST.value()
                ));
            }

            UserQrCode qr = qrCodeService.regenerateQrCode(request.getUserId());

            return ResponseEntity.ok(
                    new QrCodeResponseDTO("QR regenerado exitosamente", qr.getQrCode())
            );
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "message", e.getReason(),
                    "status", e.getStatusCode().value()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "status", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPaymentPassword(@RequestBody SetPasswordRequest request) {
        try {
            if (request.getUserId() == null || request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "userId y password son obligatorios.",
                        "status", HttpStatus.BAD_REQUEST.value()
                ));
            }

            UserQrCode qr = qrRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR no encontrado"));

            if (qr.getPaymentPassword() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya hay una contraseña establecida");
            }

            qr.setPaymentPassword(hashPassword(request.getPassword()));
            qrRepository.save(qr);

            return ResponseEntity.ok(Map.of(
                    "message", "Contraseña de pago establecida correctamente"
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "message", e.getReason(),
                    "status", e.getStatusCode().value()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "status", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody ValidatePasswordRequest request) {
        try {
            if (request.getUserId() == null || request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "userId y password son obligatorios.",
                        "status", HttpStatus.BAD_REQUEST.value()
                ));
            }

            UserQrCode qr = qrRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR no encontrado"));

            String hashed = hashPassword(request.getPassword());

            if (!hashed.equals(qr.getPaymentPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta");
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Contraseña válida"
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "message", e.getReason(),
                    "status", e.getStatusCode().value()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "status", HttpStatus.BAD_REQUEST.value()
            ));
        }
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