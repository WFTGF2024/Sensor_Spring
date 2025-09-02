package com.example.sensor_spring.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordUtil {
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int SALT_LEN = 16;
    private static final int KEY_LEN = 256; // bits
    private static final int DEFAULT_ITER = 260000;

    public static String hash(String password) { return hash(password, DEFAULT_ITER); }

    public static String hash(String password, int iterations) {
        byte[] salt = new byte[SALT_LEN];
        new SecureRandom().nextBytes(salt);
        byte[] dk = pbkdf2(password.toCharArray(), salt, iterations, KEY_LEN);
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashHex = toHex(dk);
        return String.format("pbkdf2:sha256:%d$%s$%s", iterations, saltB64, hashHex);
    }

    public static boolean verify(String password, String stored) {
        try {
            if (stored == null) return false;
            String[] parts = stored.split("\\$");
            if (parts.length != 3) return false;
            String meta = parts[0]; // pbkdf2:sha256:iter
            String saltB64 = parts[1];
            String hashHex = parts[2];
            String[] metaParts = meta.split(":");
            if (metaParts.length < 3) return false;
            int iter = Integer.parseInt(metaParts[2]);
            byte[] salt = Base64.getDecoder().decode(saltB64);
            byte[] calc = pbkdf2(password.toCharArray(), salt, iter, KEY_LEN);
            String calcHex = toHex(calc);
            return slowEquals(hashHex, calcHex);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLenBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 not available", e);
        }
    }

    private static boolean slowEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
