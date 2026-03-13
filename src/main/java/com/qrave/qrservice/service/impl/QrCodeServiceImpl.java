package com.qrave.qrservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.qrave.qrservice.dto.QrSubscriptionDataDTO;
import com.qrave.qrservice.model.User;
import com.qrave.qrservice.model.UserQrCode;
import com.qrave.qrservice.model.UserSubscription;
import com.qrave.qrservice.repository.UserQrCodeRepository;
import com.qrave.qrservice.repository.UserRepository;
import com.qrave.qrservice.repository.UserSubscriptionRepository;
import com.qrave.qrservice.service.QrCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class QrCodeServiceImpl implements QrCodeService {

    private static final String DEFAULT_NEQUI_CODE = "NIT_1";

    private final UserQrCodeRepository repository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Value("${qr.encryption.key}")
    private String encryptionKey;

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

        if (existing.isPresent()) {
            return existing.get();
        }

        validateUserHasActiveSubscription(userId);

        String base64Qr = buildQrBase64ForUser(userId);

        UserQrCode qr = new UserQrCode();
        qr.setUserId(userId);
        qr.setQrCode(base64Qr);
        qr.setLastRegeneration(LocalDateTime.now());
        qr.setPaymentPassword(null);

        return repository.save(qr);
    }

    @Override
    public UserQrCode getQrCode(Long userId) {
        UserQrCode qr = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Código QR no encontrado"));

        // Validación básica para no devolver QR a usuarios sin suscripción activa local
        validateUserHasActiveSubscription(userId);

        return qr;
    }

    @Override
    public UserQrCode regenerateQrCode(Long userId) {
        UserQrCode qr = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Código QR no encontrado"));

        if (qr.getLastRegeneration() != null
                && qr.getLastRegeneration().plusHours(24).isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "El código QR solo puede regenerarse una vez cada 24 horas."
            );
        }

        validateUserHasActiveSubscription(userId);

        String base64Qr = buildQrBase64ForUser(userId);

        qr.setQrCode(base64Qr);
        qr.setLastRegeneration(LocalDateTime.now());
        qr.setPaymentPassword(null);

        return repository.save(qr);
    }

    private void validateUserHasActiveSubscription(Long userId) {
        UserSubscription latestSubscription = userSubscriptionRepository
                .findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna suscripción para el usuario"));

        if (latestSubscription.getStatus() == null ||
                !"ACCEPTED".equalsIgnoreCase(latestSubscription.getStatus())) {
            throw new RuntimeException("El usuario no tiene una suscripción activa de Nequi");
        }
    }

    private String buildQrBase64ForUser(Long userId) {
        QrSubscriptionDataDTO paymentData = preparePaymentDataFromUserId(userId);
        String metadataJson = convertToJson(paymentData);
        String encryptedMetadata = encrypt(metadataJson, encryptionKey);
        return generateQrBase64(encryptedMetadata);
    }

    private QrSubscriptionDataDTO preparePaymentDataFromUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo se valida que tenga una suscripción local activa.
        // Ya NO se mete el token de suscripción dentro del QR.
        UserSubscription latestSubscription = userSubscriptionRepository
                .findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No se encontró una suscripción para el usuario"));

        if (latestSubscription.getStatus() == null ||
                !"ACCEPTED".equalsIgnoreCase(latestSubscription.getStatus())) {
            throw new RuntimeException("El usuario no tiene una suscripción activa de Nequi");
        }

        QrSubscriptionDataDTO dto = new QrSubscriptionDataDTO();
        dto.setUserId(userId);
        dto.setFullName(user.getFullName());
        dto.setCode(DEFAULT_NEQUI_CODE);

        return dto;
    }

    private String convertToJson(QrSubscriptionDataDTO dto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo metadata a JSON", e);
        }
    }

    private String generateQrBase64(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Error al generar el código QR", e);
        }
    }

    private String encrypt(String data, String secretKey) {
        try {
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar", e);
        }
    }
}