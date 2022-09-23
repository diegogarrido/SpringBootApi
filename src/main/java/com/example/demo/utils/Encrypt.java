package com.example.demo.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Encrypt {
    /* Declaration of variables */
    private static final Random random = new SecureRandom();
    private static final String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int iterations = 10000;
    private static final int keyLength = 256;

    /* Method to generate the salt value. */
    public static String getSaltValue(int length) {
        StringBuilder finalVal = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            finalVal.append(characters.charAt(random.nextInt(characters.length())));
        }

        return new String(finalVal);
    }

    /* Method to generate the hash value */
    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    /* Method to encrypt the password using the original password and salt value. */
    public static String generateSecurePassword(String password, String salt) {
        String finalVal = null;

        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());

        finalVal = Base64.getEncoder().encodeToString(securePassword);

        return finalVal;
    }

    /* Method to verify if both password matches or not */
    public static boolean verifyUserPassword(String providedPassword, String securedPassword, String salt) {
        boolean finalVal = false;

        /* Generate New secure password with the same salt */
        String newSecurePassword = generateSecurePassword(providedPassword, salt);

        /* Check if two passwords are equal */
        finalVal = newSecurePassword.equalsIgnoreCase(securedPassword);

        return finalVal;
    }
}
