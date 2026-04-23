package com.example.pr_1_file_dupe.utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

/**
 * SoundManager - Handles all sound effects in the application
 * Provides easy-to-use methods for playing UI feedback sounds
 */
public class SoundManager {

    private static boolean soundEnabled = true;
    private static double volume = 0.5; // 50% volume by default

    public enum Sound {
        SCAN_START("scan_start.mp3"),
        SCAN_COMPLETE("scan_compleate.mp3"),
        DELETE_FILE("delete.mp3"),
        BUTTON_CLICK("click.mp3"),
        ERROR("error.mp3"),
        SUCCESS("success.mp3"),
        NAVIGATION("nav.mp3");

        private final String filename;

        Sound(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }
    }

    /**
     * Play a sound effect
     * @param sound The sound to play
     */
    public static void play(Sound sound) {
        if (!soundEnabled) return;

        try {
            URL resource = SoundManager.class.getResource(
                    "/com/example/pr_1_file_dupe/sound/" + sound.getFilename()
            );

            if (resource == null) {
                System.out.println("Sound file not found: " + sound.getFilename());
                return;
            }

            Media media = new Media(resource.toString());
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(volume);
            
            // Auto-dispose after playing
            player.setOnEndOfMedia(() -> player.dispose());
            player.play();

        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    /**
     * Play sound in a separate thread to avoid blocking UI
     * @param sound The sound to play
     */
    public static void playAsync(Sound sound) {
        new Thread(() -> play(sound)).start();
    }

    /**
     * Enable or disable all sounds
     * @param enabled true to enable sounds
     */
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    /**
     * Set global volume level
     * @param vol Volume from 0.0 to 1.0
     */
    public static void setVolume(double vol) {
        volume = Math.max(0.0, Math.min(1.0, vol));
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static double getVolume() {
        return volume;
    }
}