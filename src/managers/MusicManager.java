package managers;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Manages background music and sound effects for the game
 * Singleton pattern for global music control
 */
public class MusicManager {
    
    private static MusicManager instance;
    
    private Clip backgroundMusic;
    private FloatControl volumeControl;
    private float currentVolume = 0.5f; // 50% volume by default
    private boolean isMusicEnabled = true;
    private String currentMusicFile = "";
    
    private MusicManager() {
        // Private constructor for singleton
    }
    
    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }
    
    /**
     * Play background music on loop
     */
    public void playMusic(String musicFile) {
        // Don't restart if already playing the same file
        if (currentMusicFile.equals(musicFile) && backgroundMusic != null && backgroundMusic.isRunning()) {
            return;
        }
        
        stopMusic(); // Stop any currently playing music
        
        try {
            File audioFile = new File(musicFile);
            
            if (!audioFile.exists()) {
                System.err.println("❌ Music file not found: " + musicFile);
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            
            // Get volume control
            if (backgroundMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(currentVolume);
            }
            
            // Loop continuously
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            currentMusicFile = musicFile;
            
            if (isMusicEnabled) {
                backgroundMusic.start();
                System.out.println("🎵 Playing music: " + musicFile);
            }
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("❌ Unsupported audio format: " + musicFile);
            System.err.println("💡 Make sure the file is in WAV, AIFF, or AU format");
            System.err.println("💡 Convert MP3 to WAV using online converters or software");
        } catch (IOException e) {
            System.err.println("❌ Error loading music file: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("❌ Audio line unavailable: " + e.getMessage());
        }
    }
    
    /**
     * Stop background music
     */
    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
            backgroundMusic = null;
            currentMusicFile = "";
            System.out.println("🔇 Music stopped");
        }
    }
    
    /**
     * Pause background music
     */
    public void pauseMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            System.out.println("⏸️ Music paused");
        }
    }
    
    /**
     * Resume background music
     */
    public void resumeMusic() {
        if (backgroundMusic != null && !backgroundMusic.isRunning() && isMusicEnabled) {
            backgroundMusic.start();
            System.out.println("▶️ Music resumed");
        }
    }
    
    /**
     * Set volume (0.0 to 1.0)
     */
    public void setVolume(float volume) {
        // Clamp between 0 and 1
        currentVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        if (volumeControl != null) {
            // Convert 0-1 range to decibel range
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            
            // Logarithmic scale for better perceived volume control
            float gain;
            if (currentVolume <= 0.01f) {
                gain = min; // Mute
            } else {
                // Map 0-1 to decibels (typically -80 to 6 dB)
                gain = min + (max - min) * currentVolume;
            }
            
            volumeControl.setValue(gain);
        }
    }
    
    /**
     * Get current volume (0.0 to 1.0)
     */
    public float getVolume() {
        return currentVolume;
    }
    
    /**
     * Increase volume by 10%
     */
    public void increaseVolume() {
        setVolume(currentVolume + 0.1f);
    }
    
    /**
     * Decrease volume by 10%
     */
    public void decreaseVolume() {
        setVolume(currentVolume - 0.1f);
    }
    
    /**
     * Toggle music on/off
     */
    public void toggleMusic() {
        isMusicEnabled = !isMusicEnabled;
        
        if (isMusicEnabled) {
            resumeMusic();
        } else {
            pauseMusic();
        }
        
        System.out.println("🎵 Music " + (isMusicEnabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if music is enabled
     */
    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }
    
    /**
     * Check if music is currently playing
     */
    public boolean isPlaying() {
        return backgroundMusic != null && backgroundMusic.isRunning();
    }
    
    /**
     * Get volume as percentage (0-100)
     */
    public int getVolumePercent() {
        return (int) (currentVolume * 100);
    }
    
    /**
     * Set volume from percentage (0-100)
     */
    public void setVolumePercent(int percent) {
        setVolume(percent / 100.0f);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        stopMusic();
    }
}