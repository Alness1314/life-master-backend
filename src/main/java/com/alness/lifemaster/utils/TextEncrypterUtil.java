package com.alness.lifemaster.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class TextEncrypterUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_SIZE = 128;

    private TextEncrypterUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String encrypt(String text, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // Generate a random IV
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes());

            // Prepend IV to the encrypted text
            byte[] encryptedWithIv = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, encryptedWithIv, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            log.error("Error encrypting the text", e);
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Error encrypting the text.");
        }
    }

    public static String decrypt(String encryptedText, SecretKey key) {
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

            // Extract IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH);

            byte[] encryptedBytes = new byte[encryptedWithIv.length - IV_LENGTH];
            System.arraycopy(encryptedWithIv, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("Error decrypting the text", e);
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Error decrypting the text.");
        }
    }

    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE);
            return keyGen.generateKey();
        } catch (Exception e) {
            log.error("Error generating the encryption key", e);
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Error generating the encryption key.");
        }
    }

    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey stringToKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, "AES");
    }
}
