package com.qrave.qrservice.controller;

import com.qrave.qrservice.dto.QrCodeRequestDTO;
import com.qrave.qrservice.dto.QrCodeResponseDTO;
import com.qrave.qrservice.model.UserQrCode;
import com.qrave.qrservice.service.QrCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QrCodeResponseDTO> generate(@RequestBody QrCodeRequestDTO request) {
        UserQrCode qr = qrCodeService.generateQrCode(request.getUserId());
        return ResponseEntity.ok(new QrCodeResponseDTO("QR generado exitosamente", qr.getQrCode()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<QrCodeResponseDTO> get(@PathVariable Long userId) {
        UserQrCode qr = qrCodeService.getQrCode(userId);
        return ResponseEntity.ok(new QrCodeResponseDTO("QR recuperado", qr.getQrCode()));
    }

    @PostMapping("/regenerate")
    public ResponseEntity<QrCodeResponseDTO> regenerate(@RequestBody QrCodeRequestDTO request) {
        UserQrCode qr = qrCodeService.regenerateQrCode(request.getUserId());
        return ResponseEntity.ok(new QrCodeResponseDTO("QR regenerado exitosamente", qr.getQrCode()));
    }
}


