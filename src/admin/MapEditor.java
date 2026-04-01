package admin;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import main.Game;
import managers.DatabaseManager;
import managers.SessionManager;
import managers.LevelConfiguration;
import objects.PathPoint;
import objects.Tile;

import static helpz.Constants.Tiles.*;

/**
 * ENHANCED Admin Map Editor with Enemy Spawn Configuration
 */
public class MapEditor {

    private Game game;
    private int[][] lvl;
    private Tile selectedTile;
    private int mouseX, mouseY;
    private int lastTileX, lastTileY, lastTileId;
    private boolean drawSelect;
    private AdminToolbar toolbar;
    private PathPoint start, end;

    private int currentLevelId = -1;
    private String levelName = "";
    private int difficulty = 1;
    private LevelConfiguration levelConfig;

    private boolean inputtingName = false;
    private boolean inputtingDifficulty = false;
    private boolean configuringEnemies = false; // NEW
    private String tempInput = "";
    private int configStep = 0; // For multi-step enemy config

    private boolean validPath = false;
    private String validationMessage = "";

    private static final int WORKSPACE_HEIGHT = 640;
    private static final int TOOLBAR_Y = 710;
    private static final int TOOLBAR_HEIGHT = 90;

    // Cached rendering resources
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font INFO_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font SMALL_FONT = new Font("Arial", Font.ITALIC, 10);
    private static final Font INPUT_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font INPUT_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font INPUT_HINT_FONT = new Font("Arial", Font.ITALIC, 12);
    private static final Font TILE_FONT = new Font("Arial", Font.PLAIN, 10);
    private static final Font PATH_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font CONFIG_FONT = new Font("Arial", Font.PLAIN, 11);

    private static final Color BG_COLOR = new Color(50, 50, 70);
    private static final Color TOP_BAR_COLOR = new Color(40, 40, 60, 230);
    private static final Color GOLD_COLOR = new Color(255, 215, 0);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color GRAY_COLOR = new Color(200, 200, 200);
    private static final Color GRID_COLOR = new Color(100, 100, 100, 50);
    private static final Color DIALOG_BG_COLOR = new Color(50, 50, 70);
    private static final Color DIALOG_BORDER_COLOR = new Color(255, 215, 0);
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 150);
    private static final Color SELECT_FILL_COLOR = new Color(255, 255, 0, 100);
    private static final Color SELECT_BORDER_COLOR = Color.YELLOW;
    private static final Color INFO_BG_COLOR = new Color(30, 30, 50, 200);

    private BufferedImage cachedLevelImage;
    private boolean levelDirty = true;

    public MapEditor(Game game) {
        this.game = game;
        loadDefaultLevel();
        levelConfig = new LevelConfiguration(difficulty);
        toolbar = new AdminToolbar(0, TOOLBAR_Y, 640, TOOLBAR_HEIGHT, this);
    }

    private void loadDefaultLevel() {
        lvl = new int[20][20];
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                lvl[y][x] = 0;
            }
        }

        start = new PathPoint(0, 0);
        end = new PathPoint(19, 19);
        createDefaultPath();
        levelDirty = true;
    }

    private void createDefaultPath() {
        for (int x = 0; x < 20; x++) {
            lvl[0][x] = 2;
        }
        for (int y = 0; y < 20; y++) {
            lvl[y][19] = 3;
        }
        lvl[0][19] = 4;
    }

    private void renderLevelToCache() {
        if (cachedLevelImage == null) {
            cachedLevelImage = new BufferedImage(640, 640, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g = cachedLevelImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(BG_COLOR);
        g.fillRect(0, 0, 640, 640);

        for (int y = 0; y < lvl.length; y++) {
            for (int x = 0; x < lvl[y].length; x++) {
                int id = lvl[y][x];
                try {
                    if (game.getTileManager().isSpriteAnimation(id)) {
                        g.drawImage(game.getTileManager().getAniSprite(id, 0),
                                x * 32, y * 32, null);
                    } else {
                        g.drawImage(game.getTileManager().getSprite(id),
                                x * 32, y * 32, null);
                    }
                } catch (Exception e) {
                    // Skip invalid tiles
                }
            }
        }

        g.dispose();
        levelDirty = false;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, 640, 800);

        if (levelDirty) {
            renderLevelToCache();
        }
        if (cachedLevelImage != null) {
            g2d.drawImage(cachedLevelImage, 0, 50, null);
        }

        drawSelectedTile(g);
        drawPathPoints(g);
        drawTopBar(g2d);
        drawEnemyConfigPanel(g2d); // NEW
        toolbar.draw(g);
        drawGrid(g2d);

        if (inputtingName || inputtingDifficulty || configuringEnemies) {
            drawInputDialog(g2d);
        }
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(GRID_COLOR);
        for (int x = 0; x <= 20; x++) {
            g2d.drawLine(x * 32, 50, x * 32, 50 + 20 * 32);
        }
        for (int y = 0; y <= 20; y++) {
            g2d.drawLine(0, 50 + y * 32, 20 * 32, 50 + y * 32);
        }
    }

    private void drawTopBar(Graphics2D g2d) {
        g2d.setColor(TOP_BAR_COLOR);
        g2d.fillRect(0, 0, 640, 50);

        g2d.setFont(TITLE_FONT);
        g2d.setColor(GOLD_COLOR);
        String title = currentLevelId == -1 ? "Creating New Level" : "Editing Level " + currentLevelId;
        g2d.drawString(title, 10, 20);

        g2d.setFont(INFO_FONT);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Name: " + (levelName.isEmpty() ? "[Press N]" : levelName), 10, 35);
        g2d.drawString("Diff: " + difficulty + " [D]", 200, 35);

        if (validPath) {
            g2d.setColor(SUCCESS_COLOR);
            g2d.drawString("✓ Valid Path", 300, 35);
        } else if (!validationMessage.isEmpty()) {
            g2d.setColor(ERROR_COLOR);
            g2d.drawString("✗ " + validationMessage, 300, 35);
        }

        g2d.setFont(SMALL_FONT);
        g2d.setColor(GRAY_COLOR);
        g2d.drawString("ESC: Cancel | N: Name | D: Difficulty | E: Enemy Config | V: Validate", 350, 20);
    }

    // NEW: Enemy configuration panel
    private void drawEnemyConfigPanel(Graphics2D g2d) {
        // Info panel on the right side
        g2d.setColor(INFO_BG_COLOR);
        g2d.fillRoundRect(645, 50, 150, 250, 10, 10);
        g2d.setColor(GOLD_COLOR);
        g2d.drawRoundRect(645, 50, 150, 250, 10, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(GOLD_COLOR);
        g2d.drawString("Enemy Config", 660, 70);

        g2d.setFont(CONFIG_FONT);
        g2d.setColor(Color.WHITE);

        int y = 90;
        g2d.drawString("Waves: " + levelConfig.getNumberOfWaves(), 655, y);
        y += 20;
        g2d.drawString("Base Enemies: " + levelConfig.getBaseEnemiesPerWave(), 655, y);
        y += 20;
        g2d.drawString("Increase/Wave: +" + levelConfig.getEnemyIncreasePerWave(), 655, y);
        y += 20;
        g2d.drawString("Health: " + String.format("%.1fx", levelConfig.getEnemyHealthMultiplier()), 655, y);
        y += 20;
        g2d.drawString("Speed: " + String.format("%.1fx", levelConfig.getEnemySpeedMultiplier()), 655, y);

        y += 30;
        g2d.setColor(GRAY_COLOR);
        g2d.setFont(new Font("Arial", Font.ITALIC, 9));
        g2d.drawString("Wave 1: " + levelConfig.getEnemiesForWave(1) + " enemies", 655, y);
        y += 15;
        g2d.drawString("Wave 5: " + levelConfig.getEnemiesForWave(5) + " enemies", 655, y);
        y += 15;
        g2d.drawString("Wave 10: " + levelConfig.getEnemiesForWave(10) + " enemies", 655, y);

        y += 25;
        g2d.setColor(GOLD_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("Press E to configure", 655, y);
    }

    private void drawInputDialog(Graphics2D g2d) {
        g2d.setColor(OVERLAY_COLOR);
        g2d.fillRect(0, 0, 640, 800);

        g2d.setColor(DIALOG_BG_COLOR);
        g2d.fillRoundRect(120, 250, 400, 300, 15, 15);
        g2d.setColor(DIALOG_BORDER_COLOR);
        g2d.drawRoundRect(120, 250, 400, 300, 15, 15);

        g2d.setFont(INPUT_TITLE_FONT);
        g2d.setColor(Color.WHITE);

        if (inputtingName) {
            g2d.drawString("Enter Level Name:", 160, 290);
            g2d.setFont(INPUT_FONT);
            g2d.drawString(tempInput + "_", 160, 330);
            g2d.setFont(INPUT_HINT_FONT);
            g2d.setColor(GRAY_COLOR);
            g2d.drawString("Press ENTER to confirm | ESC to cancel", 180, 370);
        } else if (inputtingDifficulty) {
            g2d.drawString("Enter Difficulty (1-3):", 160, 290);
            g2d.setFont(INPUT_FONT);
            g2d.drawString(tempInput + "_", 160, 330);
            g2d.setFont(INPUT_HINT_FONT);
            g2d.setColor(GRAY_COLOR);
            g2d.drawString("1 = Easy | 2 = Medium | 3 = Hard", 190, 360);
            g2d.drawString("Press ENTER to confirm | ESC to cancel", 180, 380);
        } else if (configuringEnemies) {
            drawEnemyConfigDialog(g2d);
        }
    }

    // NEW: Multi-step enemy configuration dialog
    private void drawEnemyConfigDialog(Graphics2D g2d) {
        g2d.setFont(INPUT_TITLE_FONT);
        g2d.setColor(GOLD_COLOR);

        String[] prompts = {
                "Number of Waves (1-20):",
                "Base Enemies per Wave (1-50):",
                "Enemy Increase per Wave (0-10):",
                "Health Multiplier (0.5-5.0):",
                "Speed Multiplier (0.5-3.0):"
        };

        String[] hints = {
                "Total waves in this level",
                "Starting enemy count in wave 1",
                "How many more enemies each wave",
                "Enemy HP multiplier (1.0 = normal)",
                "Enemy speed multiplier (1.0 = normal)"
        };

        g2d.drawString(prompts[configStep], 160, 290);

        g2d.setFont(INPUT_FONT);
        g2d.setColor(Color.WHITE);
        g2d.drawString(tempInput + "_", 160, 330);

        g2d.setFont(INPUT_HINT_FONT);
        g2d.setColor(GRAY_COLOR);
        g2d.drawString(hints[configStep], 160, 360);

        g2d.setColor(GOLD_COLOR);
        g2d.drawString("Step " + (configStep + 1) + " of 5", 160, 380);

        g2d.setColor(GRAY_COLOR);
        g2d.drawString("ENTER: Next | ESC: Cancel", 160, 400);

        // Show current values on the right
        int y = 420;
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Current Values:", 160, y);
        y += 15;
        g2d.drawString("Waves: " + levelConfig.getNumberOfWaves(), 160, y);
        y += 15;
        g2d.drawString("Base: " + levelConfig.getBaseEnemiesPerWave(), 160, y);
        y += 15;
        g2d.drawString("Increase: +" + levelConfig.getEnemyIncreasePerWave(), 160, y);
        y += 15;
        g2d.drawString("Health: " + String.format("%.1fx", levelConfig.getEnemyHealthMultiplier()), 300, 420);
        y = 435;
        g2d.drawString("Speed: " + String.format("%.1fx", levelConfig.getEnemySpeedMultiplier()), 300, y);
    }

    private void drawPathPoints(Graphics g) {
        if (start != null) {
            g.setColor(SUCCESS_COLOR);
            g.fillOval(start.getxCord() * 32 + 6, start.getyCord() * 32 + 56, 20, 20);
            g.setColor(Color.WHITE);
            g.setFont(PATH_FONT);
            g.drawString("S", start.getxCord() * 32 + 12, start.getyCord() * 32 + 70);
        }

        if (end != null) {
            g.setColor(ERROR_COLOR);
            g.fillOval(end.getxCord() * 32 + 6, end.getyCord() * 32 + 56, 20, 20);
            g.setColor(Color.WHITE);
            g.setFont(PATH_FONT);
            g.drawString("E", end.getxCord() * 32 + 13, end.getyCord() * 32 + 70);
        }
    }

    private void drawSelectedTile(Graphics g) {
        if (selectedTile != null && drawSelect) {
            g.setColor(SELECT_FILL_COLOR);
            g.fillRect(mouseX, mouseY, 32, 32);
            g.setColor(SELECT_BORDER_COLOR);
            g.drawRect(mouseX, mouseY, 32, 32);

            g.setColor(Color.WHITE);
            g.setFont(TILE_FONT);
            String tileInfo = getTileTypeName(selectedTile.getId());
            g.drawString(tileInfo, mouseX + 2, mouseY + 12);
        }
    }

    private String getTileTypeName(int id) {
        Tile tile = game.getTileManager().getTile(id);
        switch (tile.getTileType()) {
            case GRASS_TILE:
                return "GRASS";
            case WATER_TILE:
                return "WATER";
            case ROAD_TILE:
                return "ROAD";
            default:
                return "TILE";
        }
    }

    public void saveLevel() {
        if (!validateLevel()) {
            return;
        }

        if (levelName.isEmpty()) {
            validationMessage = "Set level name first (Press N)";
            return;
        }

        if (currentLevelId == -1) {
            currentLevelId = DatabaseManager.getInstance().getAllLevels().size() + 1;
        }

        int userId = SessionManager.getInstance().getUserId();

        // Use the correct saveLevel method that matches your DatabaseManager
        // First check if we should use the new method or old method
        boolean success;

        try {
            // Try the new method with enemy config
            success = DatabaseManager.getInstance().saveLevelWithConfig(
                    currentLevelId,
                    levelName,
                    lvl,
                    start.getxCord(),
                    start.getyCord(),
                    end.getxCord(),
                    end.getyCord(),
                    difficulty,
                    userId,
                    levelConfig.toConfigString());
        } catch (NoSuchMethodError | Exception e) {
            // Fallback to old method if new method doesn't exist
            System.out.println("⚠️ Using fallback save method");
            success = DatabaseManager.getInstance().saveLevel(
                    currentLevelId,
                    levelName,
                    lvl,
                    start.getxCord(),
                    start.getyCord(),
                    end.getxCord(),
                    end.getyCord(),
                    difficulty,
                    userId);
        }

        validationMessage = success ? "Saved successfully!" : "Save failed!";
    }

    public boolean validateLevel() {
        validPath = false;

        if (start == null || end == null) {
            validationMessage = "Start/End points missing";
            return false;
        }

        if (start.getxCord() < 0 || start.getxCord() >= 20 ||
                start.getyCord() < 0 || start.getyCord() >= 20 ||
                end.getxCord() < 0 || end.getxCord() >= 20 ||
                end.getyCord() < 0 || end.getyCord() >= 20) {
            validationMessage = "Start/End out of bounds";
            return false;
        }

        Tile startTile = game.getTileManager().getTile(lvl[start.getyCord()][start.getxCord()]);
        Tile endTile = game.getTileManager().getTile(lvl[end.getyCord()][end.getxCord()]);

        if (startTile.getTileType() != ROAD_TILE) {
            validationMessage = "Start must be on ROAD";
            return false;
        }

        if (endTile.getTileType() != ROAD_TILE) {
            validationMessage = "End must be on ROAD";
            return false;
        }

        int grassCount = 0;
        int roadCount = 0;
        for (int y = 0; y < lvl.length; y++) {
            for (int x = 0; x < lvl[y].length; x++) {
                Tile tile = game.getTileManager().getTile(lvl[y][x]);
                if (tile.getTileType() == GRASS_TILE)
                    grassCount++;
                if (tile.getTileType() == ROAD_TILE)
                    roadCount++;
            }
        }

        if (grassCount < 10) {
            validationMessage = "Need more GRASS for towers";
            return false;
        }

        if (roadCount < 5) {
            validationMessage = "Need more ROAD tiles";
            return false;
        }

        validPath = true;
        validationMessage = "Level is valid!";
        return true;
    }

    public void loadLevel(int levelId) {
        currentLevelId = levelId;
        var levelData = DatabaseManager.getInstance().loadLevel(levelId);

        if (levelData != null) {
            lvl = (int[][]) levelData.get("map_data");
            start = new PathPoint((int) levelData.get("start_x"), (int) levelData.get("start_y"));
            end = new PathPoint((int) levelData.get("end_x"), (int) levelData.get("end_y"));
            levelName = (String) levelData.get("level_name");
            difficulty = (int) levelData.get("difficulty");

            // Load enemy configuration if available - FIX NULL HANDLING
            String configStr = null;
            try {
                configStr = (String) levelData.get("enemy_config");
            } catch (Exception e) {
                // enemy_config column might not exist
            }

            if (configStr != null && !configStr.isEmpty()) {
                try {
                    levelConfig = LevelConfiguration.fromConfigString(configStr);
                } catch (Exception e) {
                    // Invalid config string, use defaults
                    levelConfig = new LevelConfiguration(difficulty);
                }
            } else {
                levelConfig = new LevelConfiguration(difficulty);
            }

            validateLevel();
            levelDirty = true;
        }
    }

    public void createNewLevel() {
        currentLevelId = -1;
        levelName = "";
        difficulty = 1;
        levelConfig = new LevelConfiguration(difficulty);
        validPath = false;
        validationMessage = "";
        loadDefaultLevel();
    }

    public void setSelectedTile(Tile tile) {
        this.selectedTile = tile;
        drawSelect = true;
    }

    private void changeTile(int x, int y) {
        if (selectedTile != null) {
            int tileX = x / 32;
            int tileY = (y - 50) / 32;

            if (tileY < 0 || tileY >= lvl.length || tileX < 0 || tileX >= lvl[0].length)
                return;

            if (selectedTile.getId() >= 0) {
                if (lastTileX == tileX && lastTileY == tileY && lastTileId == selectedTile.getId())
                    return;

                lastTileX = tileX;
                lastTileY = tileY;
                lastTileId = selectedTile.getId();

                lvl[tileY][tileX] = selectedTile.getId();
                levelDirty = true;

                validPath = false;
                validationMessage = "";
            } else {
                int id = lvl[tileY][tileX];
                Tile tile = game.getTileManager().getTile(id);

                if (tile.getTileType() == ROAD_TILE) {
                    if (selectedTile.getId() == -1)
                        start = new PathPoint(tileX, tileY);
                    else
                        end = new PathPoint(tileX, tileY);
                }
            }
        }
    }

    public void mouseClicked(int x, int y) {
        if (y >= TOOLBAR_Y && y < 800) {
            toolbar.mouseClicked(x, y);
        } else if (y >= 50) {
            changeTile(mouseX, mouseY);
        }
    }

    public void mouseMoved(int x, int y) {
        if (y >= TOOLBAR_Y && y < 800) {
            toolbar.mouseMoved(x, y);
            drawSelect = false;
        } else if (y >= 50 && y < TOOLBAR_Y) {
            drawSelect = true;
            mouseX = (x / 32) * 32;
            mouseY = ((y - 50) / 32) * 32 + 50;
        } else {
            drawSelect = false;
        }
    }

    public void mousePressed(int x, int y) {
        if (y >= TOOLBAR_Y && y < 800)
            toolbar.mousePressed(x, y);
    }

    public void mouseReleased(int x, int y) {
        toolbar.mouseReleased(x, y);
    }

    public void mouseDragged(int x, int y) {
        if (y >= 50 && y < TOOLBAR_Y) {
            changeTile(x, y);
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (inputtingName || inputtingDifficulty || configuringEnemies) {
            handleInputKey(e);
            return;
        }

        if (key == KeyEvent.VK_ESCAPE) {
            // Return to admin panel
        } else if (key == KeyEvent.VK_S) {
            saveLevel();
        } else if (key == KeyEvent.VK_R) {
            toolbar.rotateSprite();
        } else if (key == KeyEvent.VK_N) {
            inputtingName = true;
            tempInput = levelName;
        } else if (key == KeyEvent.VK_D) {
            inputtingDifficulty = true;
            tempInput = "" + difficulty;
        } else if (key == KeyEvent.VK_E) { // NEW
            configuringEnemies = true;
            configStep = 0;
            tempInput = "" + levelConfig.getNumberOfWaves();
        } else if (key == KeyEvent.VK_V) {
            validateLevel();
        }
    }

    private void handleInputKey(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ESCAPE) {
            inputtingName = false;
            inputtingDifficulty = false;
            configuringEnemies = false;
            configStep = 0;
            tempInput = "";
        } else if (key == KeyEvent.VK_ENTER) {
            if (inputtingName) {
                levelName = tempInput;
                inputtingName = false;
                tempInput = "";
            } else if (inputtingDifficulty) {
                try {
                    int d = Integer.parseInt(tempInput);
                    if (d >= 1 && d <= 3) {
                        difficulty = d;
                        levelConfig.setDifficulty(d); // Update config defaults
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input
                }
                inputtingDifficulty = false;
                tempInput = "";
            } else if (configuringEnemies) {
                handleEnemyConfigInput();
            }
        } else if (key == KeyEvent.VK_BACK_SPACE) {
            if (tempInput.length() > 0) {
                tempInput = tempInput.substring(0, tempInput.length() - 1);
            }
        } else {
            char c = e.getKeyChar();
            if (inputtingName && (Character.isLetterOrDigit(c) || c == ' ' || c == '-')) {
                if (tempInput.length() < 30) {
                    tempInput += c;
                }
            } else if (inputtingDifficulty && Character.isDigit(c) && tempInput.length() < 1) {
                tempInput += c;
            } else if (configuringEnemies && (Character.isDigit(c) || c == '.')) {
                if (tempInput.length() < 10) {
                    tempInput += c;
                }
            }
        }
    }

    // NEW: Handle multi-step enemy configuration
    private void handleEnemyConfigInput() {
        try {
            switch (configStep) {
                case 0: // Number of waves
                    int waves = Integer.parseInt(tempInput);
                    levelConfig.setNumberOfWaves(waves);
                    break;
                case 1: // Base enemies
                    int base = Integer.parseInt(tempInput);
                    levelConfig.setBaseEnemiesPerWave(base);
                    break;
                case 2: // Enemy increase
                    int increase = Integer.parseInt(tempInput);
                    levelConfig.setEnemyIncreasePerWave(increase);
                    break;
                case 3: // Health multiplier
                    double health = Double.parseDouble(tempInput);
                    levelConfig.setEnemyHealthMultiplier(health);
                    break;
                case 4: // Speed multiplier
                    double speed = Double.parseDouble(tempInput);
                    levelConfig.setEnemySpeedMultiplier(speed);
                    configuringEnemies = false;
                    configStep = 0;
                    tempInput = "";
                    return;
            }

            // Move to next step
            configStep++;
            if (configStep < 5) {
                // Pre-fill with current value
                switch (configStep) {
                    case 1:
                        tempInput = "" + levelConfig.getBaseEnemiesPerWave();
                        break;
                    case 2:
                        tempInput = "" + levelConfig.getEnemyIncreasePerWave();
                        break;
                    case 3:
                        tempInput = String.format("%.1f", levelConfig.getEnemyHealthMultiplier());
                        break;
                    case 4:
                        tempInput = String.format("%.1f", levelConfig.getEnemySpeedMultiplier());
                        break;
                }
            }
        } catch (NumberFormatException ex) {
            // Invalid input, stay on same step
        }
    }

    public boolean isInputting() {
        return inputtingName || inputtingDifficulty || configuringEnemies;
    }

    public Game getGame() {
        return game;
    }

    public int getCurrentLevelId() {
        return currentLevelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean isValidPath() {
        return validPath;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public LevelConfiguration getLevelConfig() { return levelConfig; }

}