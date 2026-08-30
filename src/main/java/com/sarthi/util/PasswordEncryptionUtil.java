package com.sarthi.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class PasswordEncryptionUtil {

    private static final String SECRET_SEED = "SarthiRitesSecureKey2026!@#$Enc";
    private static final String ALGORITHM = "AES";
    public static final String PREFIX = "ENC:";
    private static final SecretKeySpec secretKeySpec;

    static {
        try {
            byte[] key = SECRET_SEED.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 32); // 256-bit AES key
            secretKeySpec = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing password encryption key", e);
        }
    }

    /**
     * Checks if the password string is already encrypted.
     */
    public static boolean isEncrypted(String password) {
        return password != null && password.startsWith(PREFIX);
    }

    /**
     * Encrypts a plain-text password using AES-256 and adds the ENC: prefix.
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            return plainText;
        }
        if (isEncrypted(plainText)) {
            return plainText; // Already encrypted
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.trim().getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting password: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts an encrypted password (with ENC: prefix).
     * If the password is not encrypted, returns it as plain text.
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.trim().isEmpty()) {
            return cipherText;
        }
        if (!isEncrypted(cipherText)) {
            return cipherText; // Already plain text
        }
        try {
            String actualCipher = cipherText.substring(PREFIX.length()).trim();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(actualCipher);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Graceful fallback to avoid breaking anything
            return cipherText;
        }
    }

    /**
     * Matches a raw user input password against a stored password (plain text or encrypted).
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        String cleanRaw = rawPassword.trim();
        String cleanStored = storedPassword.trim();

        // 1. Direct match (plain text in DB)
        if (cleanRaw.equals(cleanStored)) {
            return true;
        }

        // 2. Decrypted comparison
        String decrypted = decrypt(cleanStored);
        if (cleanRaw.equals(decrypted)) {
            return true;
        }

        // 3. Encrypted comparison
        return encrypt(cleanRaw).equals(cleanStored);
    }
}
