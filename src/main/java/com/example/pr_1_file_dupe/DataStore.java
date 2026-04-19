package com.example.pr_1_file_dupe;

import java.io.*;
import java.util.Properties;

/**
 * dataStore with additional settings for:
 * - System file filtering
 * - Sound effects
 * - Last folder memory
 */
public class DataStore {
    private static final String CONFIG_FILE = "stats.properties";
    private Properties props = new Properties();

    public dataStore() {
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

    private void save() {
        try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
            props.store(os, "User Scan Statistics and Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════
    //  THEME SETTINGS
    // ════════════════════════════════════════════════
    public boolean isDarkTheme() {
        return Boolean.parseBoolean(props.getProperty("darkTheme", "false"));
    }

    public void setDarkTheme(boolean dark) {
        props.setProperty("darkTheme", String.valueOf(dark));
        save();
    }

    // ════════════════════════════════════════════════
    //  SAFETY SETTINGS
    // ════════════════════════════════════════════════
    public boolean isSafeMode() {
        return Boolean.parseBoolean(props.getProperty("safeMode", "true"));
    }

    public void setSafeMode(boolean safe) {
        props.setProperty("safeMode", String.valueOf(safe));
        save();
    }

    // ════════════════════════════════════════════════
    //  NEW: SYSTEM FILE PROTECTION
    // ════════════════════════════════════════════════
    public boolean isSkipSystemFiles() {
        return Boolean.parseBoolean(props.getProperty("skipSystemFiles", "true"));
    }

    public void setSkipSystemFiles(boolean skip) {
        props.setProperty("skipSystemFiles", String.valueOf(skip));
        save();
    }

    // ════════════════════════════════════════════════
    //  NEW: SOUND SETTINGS
    // ════════════════════════════════════════════════
    public boolean isSoundEnabled() {
        return Boolean.parseBoolean(props.getProperty("soundEnabled", "true"));
    }

    public void setSoundEnabled(boolean enabled) {
        props.setProperty("soundEnabled", String.valueOf(enabled));
        save();
    }

    public double getSoundVolume() {
        return Double.parseDouble(props.getProperty("soundVolume", "0.5"));
    }

    public void setSoundVolume(double volume) {
        props.setProperty("soundVolume", String.valueOf(volume));
        save();
    }

    // ════════════════════════════════════════════════
    //  FOLDER MEMORY
    // ════════════════════════════════════════════════
    public String getLastFolder() {
        return props.getProperty("lastFolder", "");
    }

    public void setLastFolder(String path) {
        props.setProperty("lastFolder", path);
        save();
    }

    // ════════════════════════════════════════════════
    //  STATISTICS
    // ════════════════════════════════════════════════
    public void updateStats(long spaceSaved, int groupsFound, int scannedCount) {
        long currentSaved = Long.parseLong(props.getProperty("totalSaved", "0"));
        int currentGroups = Integer.parseInt(props.getProperty("totalGroups", "0"));
        int currentScanned = Integer.parseInt(props.getProperty("totalScanned", "0"));

        props.setProperty("totalSaved", String.valueOf(currentSaved + spaceSaved));
        props.setProperty("totalGroups", String.valueOf(currentGroups + groupsFound));
        props.setProperty("totalScanned", String.valueOf(currentScanned + scannedCount));

        save();
    }

    public String getTotalSaved() { 
        return props.getProperty("totalSaved", "0"); 
    }

    public String getTotalGroups() { 
        return props.getProperty("totalGroups", "0"); 
    }

    public String getTotalScanned() { 
        return props.getProperty("totalScanned", "0"); 
    }

    // ════════════════════════════════════════════════
    //  SCAN SETTINGS
    // ════════════════════════════════════════════════
    public String getHashAlgorithm() { 
        return props.getProperty("hashAlgorithm", "SHA-256"); 
    }
    
    public void setHashAlgorithm(String algo) { 
        props.setProperty("hashAlgorithm", algo); 
        save(); 
    }

    public boolean isSkipHidden() { 
        return Boolean.parseBoolean(props.getProperty("skipHidden", "true")); 
    }
    
    public void setSkipHidden(boolean skip) { 
        props.setProperty("skipHidden", String.valueOf(skip)); 
        save(); 
    }

    public long getMinFileSizeKB() { 
        return Long.parseLong(props.getProperty("minSizeKB", "0")); 
    }
    
    public void setMinFileSizeKB(long size) { 
        props.setProperty("minSizeKB", String.valueOf(size)); 
        save(); 
    }
}