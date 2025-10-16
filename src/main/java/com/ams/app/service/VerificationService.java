package com.ams.app.service;

import java.security.SecureRandom;
import java.time.Instant;

public class VerificationService {
    private final SecureRandom random = new SecureRandom();
    private String lastCodeHash;
    private Instant expiresAt;

    public void sendOtp(String email, String mobile) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        // Simulate sending code (in real app, integrate with email/SMS API)
        System.out.println("[OTP SENT] To email: " + email + ", mobile: " + mask(mobile) + " -> Code: " + code);
        this.lastCodeHash = hash(code);
        this.expiresAt = Instant.now().plusSeconds(300); // 5 minutes expiry
    }

    public boolean verify(String inputCode) {
        if (inputCode == null) return false;
        if (expiresAt == null || Instant.now().isAfter(expiresAt)) return false;
        return hash(inputCode.trim()).equals(lastCodeHash);
    }

    private String mask(String mobile) {
        if (mobile == null || mobile.length() < 4) return "****";
        return "****" + mobile.substring(mobile.length() - 4);
    }

    private String hash(String s) {
        return Integer.toHexString(s.hashCode());
    }
}
