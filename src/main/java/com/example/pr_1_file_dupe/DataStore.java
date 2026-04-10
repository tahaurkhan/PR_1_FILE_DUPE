package com.example.pr_1_file_dupe;

import java.io.*;
import java.util.Properties;

public class DataStore {
    private static final String CONFIG_FILE = "stats.properties";
    
    // This is the 'props' variable Eclipse was looking for!
    private Properties props = new Properties();

    public DataStore() {
        load();
    }

    private void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                props.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateStats(long spaceSaved, int groupsFound, int scannedCount) {
        long currentSaved = Long.parseLong(props.getProperty("totalSaved", "0"));
        int currentGroups = Integer.parseInt(props.getProperty("totalGroups", "0"));
        int currentScanned = Integer.parseInt(props.getProperty("totalScanned", "0"));

        props.setProperty("totalSaved", String.valueOf(currentSaved + spaceSaved));
        props.setProperty("totalGroups", String.valueOf(currentGroups + groupsFound));
        props.setProperty("totalScanned", String.valueOf(currentScanned + scannedCount));

        save();
    }

    // This is the 'save()' method Eclipse was looking for!
    private void save() {
        try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
            props.store(os, "User Scan Statistics and Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- STATS GETTERS ---
    public String getTotalSaved() { return props.getProperty("totalSaved", "0"); }
    public String getTotalGroups() { return props.getProperty("totalGroups", "0"); }
    public String getTotalScanned() { return props.getProperty("totalScanned", "0"); }

    // --- SETTINGS METHODS ---
    public String getHashAlgorithm() { return props.getProperty("hashAlgorithm", "SHA-256"); }
    
    public void setHashAlgorithm(String algo) { 
        props.setProperty("hashAlgorithm", algo); 
        save(); 
    }

    public boolean isSkipHidden() { return Boolean.parseBoolean(props.getProperty("skipHidden", "true")); }
    
    public void setSkipHidden(boolean skip) { 
        props.setProperty("skipHidden", String.valueOf(skip)); 
        save(); 
    }

    public long getMinFileSizeKB() { return Long.parseLong(props.getProperty("minSizeKB", "0")); }
    
    public void setMinFileSizeKB(long size) { 
        props.setProperty("minSizeKB", String.valueOf(size)); 
        save(); 
    }
}