package com.qrave.qrservice.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrave.qrservice.dto.QrSubscriptionDataDTO;
import com.qrave.qrservice.model.User;
import com.qrave.qrservice.model.UserQrCode;
import com.qrave.qrservice.model.UserSubscription;
import com.qrave.qrservice.repository.UserQrCodeRepository;
import com.qrave.qrservice.repository.UserRepository;
import com.qrave.qrservice.repository.UserSubscriptionRepository;
import com.qrave.qrservice.service.QrCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class QrCodeServiceImpl implements QrCodeService {

    private final UserQrCodeRepository repository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public QrCodeServiceImpl(UserQrCodeRepository repository,
                             UserRepository userRepository,
                             UserSubscriptionRepository userSubscriptionRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @Override
    public UserQrCode generateQrCode(Long userId) {
        Optional<UserQrCode> existing = repository.findByUserId(userId);
        if (existing.isPresent()) return existing.get();

        // 🔥 Preparar metadata real
        QrSubscriptionDataDTO paymentData = preparePaymentDataFromUserId(userId);
        String metadataJson = convertToJson(paymentData);

        // 🔥 Generar QR con metadata
        String base64Qr = generateQrBase64(metadataJson);

        UserQrCode qr = new UserQrCode();
        qr.setUserId(userId);
        qr.setQrCode(base64Qr);
        qr.setLastRegeneration(LocalDateTime.now());

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código QR solo puede regenerarse una vez cada 24 horas.");
        }

        // 🔥 Preparar nueva metadata real
        QrSubscriptionDataDTO paymentData = preparePaymentDataFromUserId(userId);
        String metadataJson = convertToJson(paymentData);

        // 🔥 Generar nuevo QR
        String newBase64Qr = generateQrBase64(metadataJson);

        qr.setQrCode(newBase64Qr);
        qr.setLastRegeneration(LocalDateTime.now());

        return repository.save(qr);
    }

    // 🚀 Prepara los datos de pago
    private QrSubscriptionDataDTO preparePaymentDataFromUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserSubscription latestSubscription = userSubscriptionRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No se encontró una suscripción activa para el usuario"));

        QrSubscriptionDataDTO dto = new QrSubscriptionDataDTO();
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCode("NIT_1"); // 🔥 Código fijo
        dto.setSubscriptionToken(latestSubscription.getSubscriptionToken());
        dto.setFullName(user.getFullName());

        return dto;
    }

    // 🚀 Convierte objeto a JSON
    private String convertToJson(QrSubscriptionDataDTO dto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo metadata a JSON", e);
        }
    }

    // 🚀 Genera QR base64
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
