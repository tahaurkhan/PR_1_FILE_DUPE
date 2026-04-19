package com.example.pr_1_file_dupe.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SystemFileFilter - Identifies system files, folders, and locked files
 * Prevents scanning of critical OS directories and locked resources
 */
public class SystemFileFilter {

    // ═══════════════════════════════════════════════
    //  WINDOWS SYSTEM PATHS
    // ═══════════════════════════════════════════════
    private static final Set<String> WINDOWS_SYSTEM_DIRS = new HashSet<>(Arrays.asList(
        "Windows", "Program Files", "Program Files (x86)", "ProgramData",
        "System Volume Information", "$Recycle.Bin", "$RECYCLE.BIN",
        "Recovery", "Boot", "Config.Msi", "AppData\\Local\\Temp",
        "MSOCache", "PerfLogs", "WinSxS", "Installer"
    ));

    private static final Set<String> WINDOWS_SYSTEM_FILES = new HashSet<>(Arrays.asList(
        "ntuser.dat", "ntuser.dat.log", "NTUSER.DAT", "pagefile.sys", "swapfile.sys",
        "hiberfil.sys", "bootmgr", "desktop.ini", "thumbs.db", "Thumbs.db"
    ));

    // ═══════════════════════════════════════════════
    //  LINUX SYSTEM PATHS
    // ═══════════════════════════════════════════════
    private static final Set<String> LINUX_SYSTEM_DIRS = new HashSet<>(Arrays.asList(
        "/bin", "/boot", "/dev", "/etc", "/lib", "/lib64", "/proc", "/root",
        "/run", "/sbin", "/sys", "/tmp", "/var/cache", "/var/log", "/var/tmp",
        "/usr/bin", "/usr/sbin", "/usr/lib", "/usr/lib64", "/snap",
        ".cache", ".config", ".local", ".mozilla", ".gvfs", ".dbus"
    ));

    private static final Set<String> LINUX_SYSTEM_FILES = new HashSet<>(Arrays.asList(
        ".bashrc", ".bash_history", ".profile", ".Xauthority", ".ICEauthority",
        ".gvfs", ".recently-used"
    ));

    /**
     * Check if a file or folder is a system resource
     * @param file The file to check
     * @return true if it's a system file/folder
     */
    public static boolean isSystemFile(File file) {
        if (file == null || !file.exists()) return false;

        String name = file.getName();
        String absolutePath = file.getAbsolutePath();
        String os = System.getProperty("os.name").toLowerCase();

        // Check based on OS
        if (os.contains("win")) {
            return isWindowsSystemFile(file, name, absolutePath);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return isLinuxSystemFile(file, name, absolutePath);
        }

        return false;
    }

    /**
     * Windows system file detection
     */
    private static boolean isWindowsSystemFile(File file, String name, String path) {
        // Check system directories
        for (String sysDir : WINDOWS_SYSTEM_DIRS) {
            if (path.contains("\\" + sysDir + "\\") || path.endsWith("\\" + sysDir)) {
                return true;
            }
        }

        // Check system files
        if (WINDOWS_SYSTEM_FILES.contains(name)) {
            return true;
        }

        // Check file attributes (hidden, system)
        try {
            if (file.isHidden()) return true;
            
            // Check for Windows system attribute
            Path p = file.toPath();
            if (Files.isHidden(p)) return true;
            
        } catch (Exception e) {
            // If we can't read attributes, assume it's protected
            return true;
        }

        return false;
    }

    /**
     * Linux system file detection
     */
    private static boolean isLinuxSystemFile(File file, String name, String path) {
        // Check system directories
        for (String sysDir : LINUX_SYSTEM_DIRS) {
            if (path.startsWith(sysDir + "/") || path.equals(sysDir)) {
                return true;
            }
        }

        // Check system files
        if (LINUX_SYSTEM_FILES.contains(name)) {
            return true;
        }

        // Skip hidden files in system directories
        if (name.startsWith(".") && isInSystemDirectory(path)) {
            return true;
        }

        return false;
    }

    /**
     * Check if file is locked (cannot be accessed)
     * @param file The file to check
     * @return true if file is locked
     */
    public static boolean isFileLocked(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }

        // Try to check if file is readable and writable
        try {
            Path path = file.toPath();
            
            // If we can't read, it's locked
            if (!Files.isReadable(path)) {
                return true;
            }

            // Additional check: try to get file channel (Windows lock detection)
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
                    java.nio.channels.FileChannel channel = raf.getChannel();
                    try {
                        channel.tryLock(0, Long.MAX_VALUE, true);
                    } catch (java.nio.channels.OverlappingFileLockException e) {
                        return true; // File is locked by another process
                    }
                } catch (Exception e) {
                    return true; // Can't access = locked
                }
            }

        } catch (Exception e) {
            return true; // Any error = assume locked
        }

        return false;
    }

    /**
     * Check if path is inside a system directory
     */
    private static boolean isInSystemDirectory(String path) {
        for (String sysDir : LINUX_SYSTEM_DIRS) {
            if (path.startsWith(sysDir + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if folder contains program files (for duplicate program detection)
     * @param folder The folder to check
     * @return true if it looks like a program installation directory
     */
    public static boolean isProgramFolder(File folder) {
        if (!folder.isDirectory()) return false;

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return false;

        // Look for executable files and common program indicators
        boolean hasExecutable = false;
        boolean hasConfigFiles = false;

        for (File file : files) {
            String name = file.getName().toLowerCase();

            // Check for executables
            if (name.endsWith(".exe") || name.endsWith(".app") || name.endsWith(".bin")) {
                hasExecutable = true;
            }

            // Check for config/manifest files
            if (name.equals("manifest.json") || name.equals("package.json") || 
                name.equals("setup.exe") || name.equals("uninstall.exe") ||
                name.contains("readme") || name.contains("license")) {
                hasConfigFiles = true;
            }
        }

        // If it has both executables and config files, it's likely a program folder
        return hasExecutable && hasConfigFiles;
    }
}