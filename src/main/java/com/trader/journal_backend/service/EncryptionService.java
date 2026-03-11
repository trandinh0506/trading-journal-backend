package com.trader.journal_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import java.util.Base64;

@Slf4j
@Service
public class EncryptionService {

    @Value("${app.security.encrypt-key}") 
    private String secretKey;

    private static final String ALGORITHM = "AES";

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() != 16) {
            log.error("ENCRYPTION_INIT_FAILED | AES key must be exactly 16 characters!");
            throw new RuntimeException("Invalid encryption key length");
        }
        log.info("ENCRYPTION_SERVICE_READY | Algorithm: {}", ALGORITHM);
    }

    public String encrypt(String data) {
        if (data == null) return null;
        try {
            SecretKeySpec spec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            log.error("ENCRYPTION_ERROR | Operation: ENCRYPT | Error: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            SecretKeySpec spec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, spec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception e) {
            log.error("DECRYPTION_ERROR | Operation: DECRYPT | Error: {}", e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }
}