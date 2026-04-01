package scenes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map;

import enemies.Enemy;
import main.Game;
import managers.DatabaseManager;
import managers.EnemyManager;
import managers.LevelConfiguration;
import managers.LevelProgressionManager;
import managers.ProjectileManager;
import managers.SessionManager;
import managers.TowerManager;
import managers.WaveManager;
import managers.MusicManager;
import objects.PathPoint;
import objects.Tower;
import ui.ActionBar;
import static helpz.Constants.Tiles.GRASS_TILE;
import static main.GameStates.*;

public class Playing extends GameScene implements SceneMethods {

    private int[][] lvl;

    private ActionBar actionBar;
    private int mouseX, mouseY;
    private EnemyManager enemyManager;
    private TowerManager towerManager;
    private ProjectileManager projManager;
    private WaveManager waveManager;
    private PathPoint start, end;
    private Tower selectedTower;
    private int goldTick;
    private boolean gamePaused;
    private MusicManager musicManager;

    private int currentLevel = 1;
    private int enemiesKilled = 0;
    private int score = 0;
    private LevelProgressionManager progressManager;

    // NEW: Level configuration
    private LevelConfiguration levelConfig;

    private boolean levelLoaded = false;

    private boolean gameInProgress = false;
    private int savedLevel = 1;
    private int savedGold = 100;
    private int savedLives = 25;
    private int savedEnemiesKilled = 0;
    private int savedScore = 0;

    public Playing(Game game) {
        super(game);

        musicManager = MusicManager.getInstance();
        progressManager = new LevelProgressionManager();
        loadFallbackLevel();

        actionBar = new ActionBar(0, 640, 640, 160, this);
        enemyManager = new EnemyManager(this, start, end);
        towerManager = new TowerManager(this);
        projManager = new ProjectileManager(this);
        waveManager = new WaveManager(this);

        startBackgroundMusic();
    }

    private void loadFallbackLevel() {
        int[][] fallbackMap = new int[20][20];
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                fallbackMap[y][x] = 0;
            }
        }

        for (int x = 0; x < 20; x++)
            fallbackMap[0][x] = 2;
        for (int y = 0; y < 20; y++)
            fallbackMap[y][19] = 3;
        fallbackMap[0][19] = 4;

        this.lvl = fallbackMap;
        this.start = new PathPoint(0, 0);
        this.end = new PathPoint(19, 19);
        this.levelLoaded = false;
        this.levelConfig = new LevelConfiguration(1); // Default easy config
    }

    private void startBackgroundMusic() {

        musicManager.playMusic("res/gameboy.wav");

    }

    public void triggerGameOver() {
        // Stop gameplay music
        musicManager.pauseMusic();

        // Switch to game over state
        setGameState(GAME_OVER);

        // Start game over music
        game.getGameOver().startGameOverMusic();
    }

    /**
     * FIXED: Load level from database with enemy configuration
     */
    public void loadDatabaseLevel(int levelId) {
        this.currentLevel = levelId;
        this.savedLevel = levelId;
        this.enemiesKilled = 0;
        this.score = 0;

        if (levelId > 0) {
            this.gameInProgress = true;
        }

        Map<String, Object> levelData = DatabaseManager.getInstance().loadLevel(levelId);

        if (levelData != null) {
            this.lvl = (int[][]) levelData.get("map_data");
            this.start = new PathPoint((int) levelData.get("start_x"), (int) levelData.get("start_y"));
            this.end = new PathPoint((int) levelData.get("end_x"), (int) levelData.get("end_y"));
            this.levelLoaded = true;

            // CRITICAL FIX: Load enemy configuration from database
            String enemyConfigStr = (String) levelData.get("enemy_config");
            if (enemyConfigStr != null && !enemyConfigStr.isEmpty()) {
                this.levelConfig = LevelConfiguration.fromConfigString(enemyConfigStr);
                System.out.println("✅ Loaded level config: " + levelConfig);
            } else {
                // Fallback to difficulty-based config
                int difficulty = (int) levelData.get("difficulty");
                this.levelConfig = new LevelConfiguration(difficulty);
                System.out.println("⚠️ No enemy_config found, using difficulty " + difficulty);
            }
        } else {
            loadFallbackLevel();
            this.levelLoaded = false;
        }

        // CRITICAL FIX: Recreate managers with proper configuration
        this.enemyManager = new EnemyManager(this, start, end);
        this.towerManager = new TowerManager(this);
        this.projManager = new ProjectileManager(this);
        this.waveManager = new WaveManager(this);

        // CRITICAL FIX: Initialize waves with configuration
        this.waveManager.initializeWithConfig(levelConfig);

        // CRITICAL FIX: Set multipliers on enemy manager
        this.enemyManager.setMultipliers(
                levelConfig.getEnemyHealthMultiplier(),
                levelConfig.getEnemySpeedMultiplier());

        System.out.println("🎮 Level " + levelId + " loaded:");
        System.out.println("   Waves: " + levelConfig.getNumberOfWaves());
        System.out.println("   Base Enemies: " + levelConfig.getBaseEnemiesPerWave());
        System.out.println("   Health Multiplier: " + levelConfig.getEnemyHealthMultiplier() + "x");
        System.out.println("   Speed Multiplier: " + levelConfig.getEnemySpeedMultiplier() + "x");

        startBackgroundMusic();
    }

    public ArrayList<Map<String, Object>> getAvailableLevels() {
        return DatabaseManager.getInstance().getAllLevels();
    }

    public void setLevel(int[][] lvl) {
        this.lvl = lvl;
    }

    public void update() {
        updateTick();

        if (!gamePaused) {
            waveManager.update();

            goldTick++;
            if (goldTick % (60 * 3) == 0)
                actionBar.addGold(1);

            if (isAllEnemiesDead()) {
                if (isThereMoreWaves()) {
                    waveManager.startWaveTimer();
                    if (isWaveTimerOver()) {
                        waveManager.increaseWaveIndex();
                        enemyManager.getEnemies().clear();
                        waveManager.resetEnemyIndex();
                    }
                }
            }

            if (isTimeForNewEnemy()) {
                if (!waveManager.isWaveTimerOver())
                    spawnEnemy();
            }

            enemyManager.update();
            towerManager.update();
            projManager.update();
        }

        if (isAllEnemiesDead() && !isThereMoreWaves()) {
            showVictoryScreen();
        }
    }

    private boolean isWaveTimerOver() {
        return waveManager.isWaveTimerOver();
    }

    private boolean isThereMoreWaves() {
        return waveManager.isThereMoreWaves();
    }

    private boolean isAllEnemiesDead() {
        if (waveManager.isThereMoreEnemiesInWave())
            return false;

        for (Enemy e : enemyManager.getEnemies())
            if (e.isAlive())
                return false;

        return true;
    }

    private void spawnEnemy() {
        enemyManager.spawnEnemy(waveManager.getNextEnemy());
    }

    private boolean isTimeForNewEnemy() {
        if (waveManager.isTimeForNewEnemy()) {
            if (waveManager.isThereMoreEnemiesInWave())
                return true;
        }
        return false;
    }

    public void setSelectedTower(Tower selectedTower) {
        this.selectedTower = selectedTower;
    }

    @Override
    public void render(Graphics g) {
        if (lvl == null) {
            g.setColor(Color.RED);
            g.drawString("ERROR: Level not loaded!", 100, 100);
            return;
        }

        drawLevel(g);
        actionBar.draw(g);
        enemyManager.draw(g);
        towerManager.draw(g);
        projManager.draw(g);

        drawSelectedTower(g);
        drawHighlight(g);
    }

    private void drawHighlight(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawRect(mouseX, mouseY, 32, 32);
    }

    private void drawSelectedTower(Graphics g) {
        if (selectedTower != null)
            g.drawImage(towerManager.getTowerImgs()[selectedTower.getTowerType()], mouseX, mouseY, null);
    }

    private void drawLevel(Graphics g) {
        if (lvl == null) {
            return;
        }

        for (int y = 0; y < lvl.length; y++) {
            for (int x = 0; x < lvl[y].length; x++) {
                int id = lvl[y][x];

                if (isAnimation(id)) {
                    g.drawImage(getSprite(id, animationIndex), x * 32, y * 32, null);
                } else {
                    g.drawImage(getSprite(id), x * 32, y * 32, null);
                }
            }
        }
    }

    public int getTileType(int x, int y) {
        int xCord = x / 32;
        int yCord = y / 32;

        if (xCord < 0 || xCord > 19)
            return 0;
        if (yCord < 0 || yCord > 19)
            return 0;

        int id = lvl[y / 32][x / 32];
        return game.getTileManager().getTile(id).getTileType();
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (y >= 640) {
            actionBar.mouseClicked(x, y);
        } else {
            actionBar.clearDisplayedTower();

            if (selectedTower != null) {
                if (isTileGrass(mouseX, mouseY)) {
                    if (getTowerAt(mouseX, mouseY) == null) {
                        towerManager.addTower(selectedTower, mouseX, mouseY);
                        removeGold(selectedTower.getTowerType());
                        selectedTower = null;
                    }
                }
            } else {
                Tower t = getTowerAt(mouseX, mouseY);
                actionBar.displayTower(t);
            }
        }
    }

    // FIND THIS METHOD IN Playing.java AND REPLACE IT:

    public void showVictoryScreen() {
        int livesRemaining = actionBar.getLives();
        int goldRemaining = actionBar.getGold();
        score = enemiesKilled * 100 + livesRemaining * 500 + goldRemaining * 10;

        int stars = progressManager.calculateStars(livesRemaining, 25);

        if (SessionManager.getInstance().isLoggedIn()) {
            int userId = SessionManager.getInstance().getUserId();
            DatabaseManager.getInstance().updateProgress(userId, currentLevel, stars, score, enemiesKilled);
        }

        this.savedGold = goldRemaining;
        this.savedLives = livesRemaining;
        this.savedEnemiesKilled = enemiesKilled;
        this.savedScore = score;

        // PAUSE MUSIC ON VICTORY
        musicManager.pauseMusic();

        game.getVictory().setLevelStats(currentLevel, enemiesKilled, score);
        setGameState(VICTORY);
    }

    private void removeGold(int towerType) {
        actionBar.payForTower(towerType);
    }

    public void upgradeTower(Tower displayedTower) {
        towerManager.upgradeTower(displayedTower);
    }

    public void removeTower(Tower displayedTower) {
        towerManager.removeTower(displayedTower);
    }

    private Tower getTowerAt(int x, int y) {
        return towerManager.getTowerAt(x, y);
    }

    private boolean isTileGrass(int x, int y) {
        int id = lvl[y / 32][x / 32];
        int tileType = game.getTileManager().getTile(id).getTileType();
        return tileType == GRASS_TILE;
    }

    public void shootEnemy(Tower t, Enemy e) {
        projManager.newProjectile(t, e);
    }

    public void setGamePaused(boolean gamePaused) {
        this.gamePaused = gamePaused;

        // PAUSE/RESUME MUSIC with game
        if (gamePaused) {
            musicManager.pauseMusic();
        } else {
            musicManager.resumeMusic();
        }
    }   

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            selectedTower = null;
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (y >= 640)
            actionBar.mouseMoved(x, y);
        else {
            mouseX = (x / 32) * 32;
            mouseY = (y / 32) * 32;
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (y >= 640)
            actionBar.mousePressed(x, y);
    }

    @Override
    public void mouseReleased(int x, int y) {
        actionBar.mouseReleased(x, y);
    }

    @Override
    public void mouseDragged(int x, int y) {
    }

    public void rewardPlayer(int enemyType) {
        int baseReward = helpz.Constants.Enemies.GetReward(enemyType);
        int scaledReward = progressManager.getScaledReward(baseReward, currentLevel);
        actionBar.addGold(scaledReward);
        enemiesKilled++;
    }

    public TowerManager getTowerManager() {
        return towerManager;
    }

    public EnemyManager getEnemyManger() {
        return enemyManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public void removeOneLife() {
        actionBar.removeOneLife();
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public LevelProgressionManager getProgressManager() {
        return progressManager;
    }

    /**
     * NEW: Get current level configuration
     */
    public LevelConfiguration getLevelConfig() {
        return levelConfig;
    }

    /**
     * FIXED: Reset with proper configuration reinitialization
     */
    public void resetEverything() {
        actionBar.resetEverything();
        enemyManager.reset();
        towerManager.reset();
        projManager.reset();
        waveManager.reset();

        mouseX = 0;
        mouseY = 0;
        selectedTower = null;
        goldTick = 0;
        gamePaused = false;

        if (gameInProgress && savedLevel == currentLevel) {
            enemiesKilled = savedEnemiesKilled;
            score = savedScore;
            actionBar.addGold(savedGold - actionBar.getGold());
        } else {
            enemiesKilled = 0;
            score = 0;
        }

        // CRITICAL FIX: Reinitialize with config
        if (levelConfig != null) {
            waveManager.initializeWithConfig(levelConfig);
            enemyManager.setMultipliers(
                    levelConfig.getEnemyHealthMultiplier(),
                    levelConfig.getEnemySpeedMultiplier());
        }
    }

    public String getCurrentLevelName() {
        Map<String, Object> levelData = DatabaseManager.getInstance().loadLevel(currentLevel);
        if (levelData != null) {
            return (String) levelData.get("level_name");
        }
        return "Level " + currentLevel;
    }

    public int getCurrentLevelDifficulty() {
        Map<String, Object> levelData = DatabaseManager.getInstance().loadLevel(currentLevel);
        if (levelData != null) {
            return (int) levelData.get("difficulty");
        }
        return 1;
    }

    public boolean isLevelLoaded() {
        return levelLoaded;
    }

    public PathPoint getStartPoint() {
        return start;
    }

    public PathPoint getEndPoint() {
        return end;
    }

    public boolean isGameInProgress() {
        return gameInProgress && levelLoaded;
    }

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
        if (!gameInProgress) {
            savedLevel = 1;
            savedGold = 100;
            savedLives = 25;
            savedEnemiesKilled = 0;
            savedScore = 0;
            levelLoaded = false;
        }
    }

    public void continueCurrentLevel() {
        if (gameInProgress && savedLevel > 0) {
            loadDatabaseLevel(savedLevel);
            resetEverything();
        } else {
            setGameState(LEVEL_SELECT);
        }
    }

    public void saveGameState() {
        savedGold = actionBar.getGold();
        savedLives = actionBar.getLives();
        savedEnemiesKilled = enemiesKilled;
        savedScore = score;
    }
}