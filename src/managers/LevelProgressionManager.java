package managers;

import java.io.*;
import java.util.ArrayList;

/**
 * Manages level progression, difficulty scaling, and player statistics.
 * Uses Polynomial Progression for difficulty scaling.
 */
public class LevelProgressionManager {
    
    private int currentLevel;
    private int maxUnlockedLevel;
    private int totalStarsEarned;
    private ArrayList<LevelStats> levelStatsList;
    
    // Difficulty scaling constants
    private static final double HEALTH_SCALE_BASE = 1.15;
    private static final double HEALTH_SCALE_POWER = 1.2;
    private static final int ENEMY_COUNT_BASE = 2;
    private static final double SPEED_SCALE_BASE = 1.08;
    private static final double REWARD_MULTIPLIER = 1.1;
    
    public LevelProgressionManager() {
        this.currentLevel = 1;
        this.maxUnlockedLevel = 1;
        this.totalStarsEarned = 0;
        this.levelStatsList = new ArrayList<>();
        loadProgress();
    }
    
    /**
     * Calculates enemy health scaling using polynomial progression
     * Formula: baseHealth × (1 + level × 0.15)^1.2
     */
    public int getScaledEnemyHealth(int baseHealth, int level) {
        double scaleFactor = Math.pow(1 + level * HEALTH_SCALE_BASE, HEALTH_SCALE_POWER);
        return (int) (baseHealth * scaleFactor);
    }
    
    /**
     * Calculates enemy count increase per level
     * Formula: baseCount + level × 2
     */
    public int getScaledEnemyCount(int baseCount, int level) {
        return baseCount + (level * ENEMY_COUNT_BASE);
    }
    
    /**
     * Calculates enemy speed scaling
     * Formula: baseSpeed × (1 + level × 0.08)
     */
    public float getScaledEnemySpeed(float baseSpeed, int level) {
        return baseSpeed * (float)(1 + level * SPEED_SCALE_BASE);
    }
    
    /**
     * Calculates gold reward scaling
     * Formula: baseReward × (1.1 ^ level)
     */
    public int getScaledReward(int baseReward, int level) {
        return (int) (baseReward * Math.pow(REWARD_MULTIPLIER, level));
    }
    
    /**
     * Get number of waves for current level using Fibonacci-like progression
     */
    public int getWavesForLevel(int level) {
        if (level <= 2) return 5 + level;
        
        int prev1 = 6; // level 1
        int prev2 = 7; // level 2
        int current = 0;
        
        for (int i = 3; i <= level; i++) {
            current = (int) ((prev1 + prev2) * 0.75); // Modified Fibonacci
            prev1 = prev2;
            prev2 = current;
        }
        
        return Math.min(current, 20); // Cap at 20 waves
    }
    
    /**
     * Calculate stars earned based on performance (1-3 stars)
     */
    public int calculateStars(int livesRemaining, int maxLives) {
        float percentage = (float) livesRemaining / maxLives;
        
        if (percentage >= 0.8f) return 3;
        if (percentage >= 0.5f) return 2;
        if (livesRemaining > 0) return 1;
        return 0;
    }
    
    /**
     * Complete a level and unlock the next one
     */
    public void completeLevel(int level, int stars, int enemiesKilled, int score) {
        // Update level stats
        LevelStats stats = getLevelStats(level);
        if (stats == null) {
            stats = new LevelStats(level);
            levelStatsList.add(stats);
        }
        
        stats.stars = Math.max(stats.stars, stars); // Keep best stars
        stats.highScore = Math.max(stats.highScore, score);
        stats.totalEnemiesKilled += enemiesKilled;
        stats.timesCompleted++;
        
        // Unlock next level
        if (level >= maxUnlockedLevel) {
            maxUnlockedLevel = level + 1;
        }
        
        totalStarsEarned += stars;
        saveProgress();
    }
    
    /**
     * Get stats for a specific level
     */
    public LevelStats getLevelStats(int level) {
        for (LevelStats stats : levelStatsList) {
            if (stats.level == level) {
                return stats;
            }
        }
        return null;
    }
    
    /**
     * Check if a level is unlocked
     */
    public boolean isLevelUnlocked(int level) {
        return level <= maxUnlockedLevel;
    }
    
    /**
     * Save progression to file
     */
    private void saveProgress() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("res/progress.dat"))) {
            writer.println(maxUnlockedLevel);
            writer.println(totalStarsEarned);
            writer.println(levelStatsList.size());
            
            for (LevelStats stats : levelStatsList) {
                writer.println(stats.level);
                writer.println(stats.stars);
                writer.println(stats.highScore);
                writer.println(stats.totalEnemiesKilled);
                writer.println(stats.timesCompleted);
            }
        } catch (IOException e) {
            System.err.println("Could not save progress: " + e.getMessage());
        }
    }
    
    /**
     * Load progression from file
     */
    private void loadProgress() {
        File progressFile = new File("res/progress.dat");
        if (!progressFile.exists()) {
            return; // First time playing
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(progressFile))) {
            maxUnlockedLevel = Integer.parseInt(reader.readLine());
            totalStarsEarned = Integer.parseInt(reader.readLine());
            int statsCount = Integer.parseInt(reader.readLine());
            
            levelStatsList.clear();
            for (int i = 0; i < statsCount; i++) {
                LevelStats stats = new LevelStats(Integer.parseInt(reader.readLine()));
                stats.stars = Integer.parseInt(reader.readLine());
                stats.highScore = Integer.parseInt(reader.readLine());
                stats.totalEnemiesKilled = Integer.parseInt(reader.readLine());
                stats.timesCompleted = Integer.parseInt(reader.readLine());
                levelStatsList.add(stats);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not load progress: " + e.getMessage());
        }
    }
    
    /**
     * Reset all progress
     */
    public void resetProgress() {
        currentLevel = 1;
        maxUnlockedLevel = 1;
        totalStarsEarned = 0;
        levelStatsList.clear();
        saveProgress();
    }
    
    // Getters and setters
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int level) { this.currentLevel = level; }
    public int getMaxUnlockedLevel() { return maxUnlockedLevel; }
    public int getTotalStarsEarned() { return totalStarsEarned; }
    
    /**
     * Inner class to store level statistics
     */
    public static class LevelStats {
        public int level;
        public int stars;
        public int highScore;
        public int totalEnemiesKilled;
        public int timesCompleted;
        
        public LevelStats(int level) {
            this.level = level;
            this.stars = 0;
            this.highScore = 0;
            this.totalEnemiesKilled = 0;
            this.timesCompleted = 0;
        }
    }

    
}
