package managers;

/**
 * Configuration class for level enemy spawning
 * Stores and manages enemy wave configuration data
 */
public class LevelConfiguration {
    
    private int numberOfWaves;
    private int baseEnemiesPerWave;
    private int enemyIncreasePerWave;
    private double enemyHealthMultiplier;
    private double enemySpeedMultiplier;
    
    /**
     * Create default configuration based on difficulty
     */
    public LevelConfiguration(int difficulty) {
        setDifficulty(difficulty);
    }
    
    /**
     * Create configuration with specific values
     */
    public LevelConfiguration(int waves, int baseEnemies, int increase, 
                            double healthMult, double speedMult) {
        this.numberOfWaves = waves;
        this.baseEnemiesPerWave = baseEnemies;
        this.enemyIncreasePerWave = increase;
        this.enemyHealthMultiplier = healthMult;
        this.enemySpeedMultiplier = speedMult;
    }
    
    /**
     * Set default values based on difficulty
     */
    public void setDifficulty(int difficulty) {
        switch (difficulty) {
            case 1: // Easy
                this.numberOfWaves = 5;
                this.baseEnemiesPerWave = 4;
                this.enemyIncreasePerWave = 2;
                this.enemyHealthMultiplier = 1.0;
                this.enemySpeedMultiplier = 1.0;
                break;
            case 2: // Medium
                this.numberOfWaves = 8;
                this.baseEnemiesPerWave = 6;
                this.enemyIncreasePerWave = 3;
                this.enemyHealthMultiplier = 1.5;
                this.enemySpeedMultiplier = 1.2;
                break;
            case 3: // Hard
                this.numberOfWaves = 12;
                this.baseEnemiesPerWave = 8;
                this.enemyIncreasePerWave = 4;
                this.enemyHealthMultiplier = 2.0;
                this.enemySpeedMultiplier = 1.5;
                break;
            default:
                this.numberOfWaves = 5;
                this.baseEnemiesPerWave = 4;
                this.enemyIncreasePerWave = 2;
                this.enemyHealthMultiplier = 1.0;
                this.enemySpeedMultiplier = 1.0;
        }
    }
    
    /**
     * Get total enemies for a specific wave
     */
    public int getEnemiesForWave(int waveNumber) {
        if (waveNumber < 1) return baseEnemiesPerWave;
        return baseEnemiesPerWave + ((waveNumber - 1) * enemyIncreasePerWave);
    }
    
    /**
     * Serialize configuration to string for database storage
     * Format: "waves,baseEnemies,increase,healthMult,speedMult"
     */
    public String toConfigString() {
        return String.format("%d,%d,%d,%.2f,%.2f", 
            numberOfWaves, 
            baseEnemiesPerWave, 
            enemyIncreasePerWave, 
            enemyHealthMultiplier, 
            enemySpeedMultiplier);
    }
    
    /**
     * Deserialize configuration from database string
     */
    public static LevelConfiguration fromConfigString(String configStr) {
        if (configStr == null || configStr.isEmpty()) {
            return new LevelConfiguration(1); // Default to easy
        }
        
        try {
            String[] parts = configStr.split(",");
            if (parts.length >= 5) {
                return new LevelConfiguration(
                    Integer.parseInt(parts[0].trim()),  // waves
                    Integer.parseInt(parts[1].trim()),  // baseEnemies
                    Integer.parseInt(parts[2].trim()),  // increase
                    Double.parseDouble(parts[3].trim()), // healthMult
                    Double.parseDouble(parts[4].trim())  // speedMult
                );
            }
        } catch (Exception e) {
            System.err.println("⚠️ Invalid config string: " + configStr);
        }
        
        return new LevelConfiguration(1); // Fallback to easy
    }
    
    // Getters
    public int getNumberOfWaves() { return numberOfWaves; }
    public int getBaseEnemiesPerWave() { return baseEnemiesPerWave; }
    public int getEnemyIncreasePerWave() { return enemyIncreasePerWave; }
    public double getEnemyHealthMultiplier() { return enemyHealthMultiplier; }
    public double getEnemySpeedMultiplier() { return enemySpeedMultiplier; }
    
    // Setters with validation
    public void setNumberOfWaves(int waves) {
        this.numberOfWaves = Math.max(1, Math.min(20, waves));
    }
    
    public void setBaseEnemiesPerWave(int base) {
        this.baseEnemiesPerWave = Math.max(1, Math.min(50, base));
    }
    
    public void setEnemyIncreasePerWave(int increase) {
        this.enemyIncreasePerWave = Math.max(0, Math.min(10, increase));
    }
    
    public void setEnemyHealthMultiplier(double mult) {
        this.enemyHealthMultiplier = Math.max(0.5, Math.min(5.0, mult));
    }
    
    public void setEnemySpeedMultiplier(double mult) {
        this.enemySpeedMultiplier = Math.max(0.5, Math.min(3.0, mult));
    }
    
    @Override
    public String toString() {
        return String.format("LevelConfig[waves=%d, base=%d, increase=%d, health=%.1fx, speed=%.1fx]",
            numberOfWaves, baseEnemiesPerWave, enemyIncreasePerWave, 
            enemyHealthMultiplier, enemySpeedMultiplier);
    }
}