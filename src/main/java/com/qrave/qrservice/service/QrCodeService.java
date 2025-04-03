package com.qrave.qrservice.service;

import com.qrave.qrservice.model.UserQrCode;

public interface QrCodeService {
    UserQrCode generateQrCode(Long userId);
    UserQrCode getQrCode(Long userId);
    UserQrCode regenerateQrCode(Long userId);
}
