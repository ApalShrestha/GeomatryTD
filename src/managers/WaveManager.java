package managers;

import java.util.ArrayList;
import java.util.Arrays;

import events.Wave;
import scenes.Playing;
import static helpz.Constants.Enemies.*;

/**
 * Wave Manager - Uses LevelConfiguration for enemy spawning
 */
public class WaveManager {

	private Playing playing;
	private ArrayList<Wave> waves = new ArrayList<>();
	private int enemySpawnTickLimit = 60 * 1;
	private int enemySpawnTick = enemySpawnTickLimit;
	private int enemyIndex, waveIndex;
	private int waveTickLimit = 60 * 5;
	private int waveTick = 0;
	private boolean waveStartTimer, waveTickTimerOver;

	private LevelConfiguration levelConfig;

	public WaveManager(Playing playing) {
		this.playing = playing;
	}

	/**
	 * CRITICAL: Initialize waves based on level configuration
	 * This should be called when level is loaded
	 */
	public void initializeWithConfig(LevelConfiguration config) {
		this.levelConfig = config;
		createWavesFromConfig();
		System.out.println("🌊 WaveManager initialized with config: " + 
			config.getNumberOfWaves() + " waves");
	}

	/**
	 * Create waves from admin-defined configuration
	 */
	private void createWavesFromConfig() {
		waves.clear();

		if (levelConfig == null) {
			System.err.println("⚠️ No level config! Using default waves");
			createDefaultWaves();
			return;
		}

		int numWaves = levelConfig.getNumberOfWaves();

		for (int wave = 1; wave <= numWaves; wave++) {
			ArrayList<Integer> enemies = new ArrayList<>();
			int enemyCount = levelConfig.getEnemiesForWave(wave);

			// Distribute enemy types based on wave progression
			double waveProgress = (double) wave / numWaves;

			for (int i = 0; i < enemyCount; i++) {
				int enemyType = selectEnemyType(wave, waveProgress, numWaves);
				enemies.add(enemyType);
			}

			waves.add(new Wave(enemies));
			
			// Debug output
			if (wave <= 3 || wave == numWaves) {
				System.out.println("   Wave " + wave + ": " + enemyCount + " enemies");
			}
		}
		
		System.out.println("✅ Created " + numWaves + " waves from configuration");
	}

	/**
	 * Select enemy type based on wave progression
	 * Early waves: Simple enemies (ORC)
	 * Mid waves: Mixed (ORC, BAT)
	 * Late waves: All types including WOLF and KNIGHT
	 */
	private int selectEnemyType(int waveNumber, double waveProgress, int totalWaves) {
		// Early game (first 20% of waves): Only ORCs
		if (waveProgress < 0.2) {
			return ORC;
		}

		// Early-mid game (20-40%): Introduce BATs
		if (waveProgress < 0.4) {
			return Math.random() < 0.3 ? BAT : ORC;
		}

		// Mid game (40-60%): Mix ORC, BAT, introduce WOLF
		if (waveProgress < 0.6) {
			double rand = Math.random();
			if (rand < 0.15)
				return WOLF;
			else if (rand < 0.5)
				return BAT;
			else
				return ORC;
		}

		// Late-mid game (60-80%): More variety
		if (waveProgress < 0.8) {
			double rand = Math.random();
			if (rand < 0.2)
				return WOLF;
			else if (rand < 0.5)
				return BAT;
			else if (rand < 0.7)
				return ORC;
			else
				return KNIGHT; // Start introducing knights
		}

		// End game (80-100%): All enemy types, more challenging
		double rand = Math.random();
		if (rand < 0.25)
			return KNIGHT;
		else if (rand < 0.5)
			return WOLF;
		else if (rand < 0.75)
			return BAT;
		else
			return ORC;
	}

	public void update() {
		if (enemySpawnTick < enemySpawnTickLimit)
			enemySpawnTick++;

		if (waveStartTimer) {
			waveTick++;
			if (waveTick >= waveTickLimit) {
				waveTickTimerOver = true;
			}
		}
	}

	public void increaseWaveIndex() {
		waveIndex++;
		waveTick = 0;
		waveTickTimerOver = false;
		waveStartTimer = false;
	}

	public boolean isWaveTimerOver() {
		return waveTickTimerOver;
	}

	public void startWaveTimer() {
		waveStartTimer = true;
	}

	public int getNextEnemy() {
		enemySpawnTick = 0;
		return waves.get(waveIndex).getEnemyList().get(enemyIndex++);
	}

	/**
	 * Fallback: Create default waves for level 1
	 */
	private void createDefaultWaves() {
		waves.add(new Wave(new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0))));
		waves.add(new Wave(new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0, 0))));
		waves.add(new Wave(new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0, 1))));
		waves.add(new Wave(new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 1, 1))));
		waves.add(new Wave(new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1))));
		System.out.println("⚠️ Using default wave configuration");
	}

	public ArrayList<Wave> getWaves() {
		return waves;
	}

	public boolean isTimeForNewEnemy() {
		return enemySpawnTick >= enemySpawnTickLimit;
	}

	public boolean isThereMoreEnemiesInWave() {
		return enemyIndex < waves.get(waveIndex).getEnemyList().size();
	}

	public boolean isThereMoreWaves() {
		return waveIndex + 1 < waves.size();
	}

	public void resetEnemyIndex() {
		enemyIndex = 0;
	}

	public int getWaveIndex() {
		return waveIndex;
	}

	public float getTimeLeft() {
		float ticksLeft = waveTickLimit - waveTick;
		return ticksLeft / 60.0f;
	}

	public boolean isWaveTimerStarted() {
		return waveStartTimer;
	}

	/**
	 * FIXED: Reset with configuration
	 */
	public void reset() {
		waves.clear();
		if (levelConfig != null) {
			createWavesFromConfig();
		} else {
			createDefaultWaves();
		}
		enemyIndex = 0;
		waveIndex = 0;
		waveStartTimer = false;
		waveTickTimerOver = false;
		waveTick = 0;
		enemySpawnTick = enemySpawnTickLimit;
	}

	/**
	 * Get the level configuration (for UI display)
	 */
	public LevelConfiguration getLevelConfig() {
		return levelConfig;
	}

	/**
	 * Get health multiplier from configuration
	 */
	public double getHealthMultiplier() {
		return levelConfig != null ? levelConfig.getEnemyHealthMultiplier() : 1.0;
	}

	/**
	 * Get speed multiplier from configuration
	 */
	public double getSpeedMultiplier() {
		return levelConfig != null ? levelConfig.getEnemySpeedMultiplier() : 1.0;
	}
}