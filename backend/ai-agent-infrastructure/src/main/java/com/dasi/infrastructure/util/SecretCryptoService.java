package com.dasi.infrastructure.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecretCryptoService {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int NONCE_SIZE = 12;

    @Value("${secret.crypto.master-key:dasi-agent-default-master-key}")
    private String masterKey;

    @Value("${secret.crypto.key-version:v1}")
    private String keyVersion;

    public EncryptedSecret encrypt(String plainText) {
        try {
            byte[] nonce = new byte[NONCE_SIZE];
            new SecureRandom().nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(deriveKey(masterKey), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return EncryptedSecret.builder()
                    .ciphertext(Base64.getEncoder().encodeToString(cipherBytes))
                    .nonce(Base64.getEncoder().encodeToString(nonce))
                    .algo("AES_GCM")
                    .keyVersion(keyVersion)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("密钥加密失败", e);
        }
    }

    public String decrypt(String ciphertext, String nonce) {
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);
            byte[] nonceBytes = Base64.getDecoder().decode(nonce);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(deriveKey(masterKey), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, nonceBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("密钥解密失败", e);
        }
    }

    private byte[] deriveKey(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("密钥派生失败", e);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EncryptedSecret {
        private String ciphertext;
        private String nonce;
        private String algo;
        private String keyVersion;
    }
}
