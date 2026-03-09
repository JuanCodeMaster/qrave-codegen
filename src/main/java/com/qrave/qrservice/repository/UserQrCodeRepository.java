package com.qrave.qrservice.repository;

import com.qrave.qrservice.model.UserQrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQrCodeRepository extends JpaRepository<UserQrCode, Long> {
    Optional<UserQrCode> findByUserId(Long userId);
}
