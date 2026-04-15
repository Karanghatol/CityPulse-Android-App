package com.citypulse.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtils — simple SHA-256 + salt hashing.
 * No external library needed for local auth.
 */
public class PasswordUtils {

    public static String hash(String password) {
        try {
            // Generate random 16-byte salt
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            String saltStr = Base64.getEncoder().encodeToString(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            String hashStr = Base64.getEncoder().encodeToString(hashed);

            return saltStr + ":" + hashStr;   // store as "salt:hash"
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static boolean verify(String password, String stored) {
        try {
            String[] parts = stored.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            String hashStr = Base64.getEncoder().encodeToString(hashed);

            return hashStr.equals(parts[1]);
        } catch (Exception e) {
            return false;
        }
    }
}
