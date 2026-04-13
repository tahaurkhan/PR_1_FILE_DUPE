package com.example.pr_1_file_dupe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    /**
     * Generates a checksum for a given file path using the specified algorithm.
     * Called by DuplicateFinder with user's chosen algorithm (SHA-256 or MD5).
     */
    public static String getFileChecksum(String filepath, String algorithm)
            throws IOException, NoSuchAlgorithmException {

        // FIX: was hardcoded "SHA-256" — now uses the algorithm parameter
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        try (FileInputStream fis = new FileInputStream(filepath)) {
            byte[] byteArray = new byte[8192]; // 8KB chunks
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        return bytesToHex(digest.digest());
    }

    /**
     * Generates a SHA-256 hash for a File object.
     * Called by FolderDuplicateFinder to compare folder contents.
     * Returns null if the file cannot be read (so the caller can skip it safely).
     */
    public static String generateHash(File f) {
        if (f == null || !f.exists() || !f.isFile()) {
            return null;
        }

        try {
            return getFileChecksum(f.getAbsolutePath(), "SHA-256");
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Could not hash file: " + f.getName() + " — " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a byte array into a readable hex string.
     * Shared by both methods above.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}