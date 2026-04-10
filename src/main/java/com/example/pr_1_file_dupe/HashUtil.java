package com.example.pr_1_file_dupe;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    // Generates a SHA-256 checksum for a given file
    public static String getFileChecksum(String filepath , String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Read the file in chunks to avoid running out of RAM on large files
        try (FileInputStream fis = new FileInputStream(filepath)) {
            byte[] byteArray = new byte[8192]; // 8KB chunks
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        // Convert the byte array into a readable Hex String
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
