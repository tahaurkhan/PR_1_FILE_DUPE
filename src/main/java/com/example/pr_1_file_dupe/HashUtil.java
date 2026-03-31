package com.example.pr_1_file_dupe;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashUtil {

    public static String generateHash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            FileInputStream fis = new FileInputStream(file);

            byte[] byteArray = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(byteArray)) != -1) {
                md.update(byteArray, 0, bytesRead);
            }
//hi  m thau
            
            fis.close();

            byte[] hashBytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}