package scenes;

import static main.GameStates.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map;

import main.Game;
import main.GameStates;
import managers.DatabaseManager;
import managers.LevelProgressionManager;
import managers.LevelProgressionManager.LevelStats;
import managers.SessionManager;
import ui.MyButton;

public class LevelSelect extends GameScene implements SceneMethods {

    private MyButton bBack;
    private ArrayList<LevelButton> levelButtons;
    private LevelProgressionManager progressManager;
    private ArrayList<Map<String, Object>> availableLevels;

    private int animTick = 0;

    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    private static final Color GRASS_GREEN = new Color(76, 175, 80);
    private static final Color LOCKED_GRAY = new Color(150, 150, 150);

    public LevelSelect(Game game) {
        super(game);
        progressManager = new LevelProgressionManager();
        loadAvailableLevels();
        initButtons();
    }

    private void loadAvailableLevels() {
        availableLevels = DatabaseManager.getInstance().getAllLevels();
        if (availableLevels.isEmpty()) {
            createDefaultLevels();
            availableLevels = DatabaseManager.getInstance().getAllLevels();
        }
    }

    private void createDefaultLevels() {
        int[][] defaultMap = new int[20][20];
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                defaultMap[y][x] = 0;
            }
        }

        for (int x = 0; x < 20; x++) {
            defaultMap[0][x] = 2;
        }
        for (int y = 0; y < 20; y++) {
            defaultMap[y][19] = 3;
        }
        defaultMap[0][19] = 4;

        DatabaseManager db = DatabaseManager.getInstance();
        if (SessionManager.getInstance().isLoggedIn()) {
            int userId = SessionManager.getInstance().getUserId();
            db.saveLevel(1, "The Beginning", defaultMap, 0, 0, 19, 19, 1, userId);
            db.saveLevel(2, "Forest Path", defaultMap, 0, 0, 19, 19, 1, userId);
            db.saveLevel(3, "Mountain Pass", defaultMap, 0, 0, 19, 19, 2, userId);
        }
    }

    private void initButtons() {
        bBack = new MyButton("Back to Menu", 20, 720, 150, 40);

        levelButtons = new ArrayList<>();

        int cols = 5;
        int rows = 4;
        int buttonSize = 100;
        int spacing = 20;
        int startX = (640 - (cols * buttonSize + (cols - 1) * spacing)) / 2;
        int startY = 150;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * cols + col;
                if (index >= availableLevels.size())
                    break;

                int x = startX + col * (buttonSize + spacing);
                int y = startY + row * (buttonSize + spacing);

                Map<String, Object> levelData = availableLevels.get(index);
                int levelId = (int) levelData.get("level_id");
                String levelName = (String) levelData.get("level_name");

                LevelButton lb = new LevelButton(x, y, buttonSize, buttonSize, levelId, levelName);
                levelButtons.add(lb);
            }
        }
    }

    public void update() {
        updateTick();
    }

    @Override
    public void render(Graphics g) {
        // CLEANED: No console spam
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        animTick++;

        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(135, 206, 235),
                0, 800, new Color(100, 180, 220));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, 640, 800);

        drawClouds(g2d);
        drawTitle(g2d);
        drawProgressInfo(g2d);
        drawLevelButtons(g2d);
        bBack.draw(g2d);
        drawInstructions(g2d);
    }

    private void drawClouds(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 120));

        int cloud1X = (animTick) % 700 - 100;
        int cloud2X = (animTick * 2) % 800 - 150;

        g2d.fillOval(cloud1X, 30, 80, 40);
        g2d.fillOval(cloud1X + 30, 20, 60, 40);
        g2d.fillOval(cloud1X + 60, 30, 70, 40);

        g2d.fillOval(cloud2X, 700, 100, 50);
        g2d.fillOval(cloud2X + 40, 690, 80, 50);
        g2d.fillOval(cloud2X + 80, 700, 90, 50);
    }

    private void drawTitle(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 48));

        g2d.setColor(new Color(0, 0, 0, 100));
        String title = "SELECT LEVEL";
        int textWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, 320 - textWidth / 2 + 3, 63);

        GradientPaint titleGradient = new GradientPaint(
                0, 40, GOLDEN_YELLOW,
                0, 80, new Color(255, 180, 0));
        g2d.setPaint(titleGradient);
        g2d.drawString(title, 320 - textWidth / 2, 60);
    }

    private void drawProgressInfo(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillRoundRect(170, 85, 300, 50, 15, 15);

        float pulse = (float) Math.sin(animTick * 0.1) * 0.3f + 0.7f;
        g2d.setColor(new Color(255, 215, 0, (int) (pulse * 200)));
        g2d.drawRoundRect(170, 85, 300, 50, 15, 15);
        g2d.setColor(DARK_BLUE);
        g2d.drawRoundRect(171, 86, 298, 48, 14, 14);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(DARK_BLUE);

        int totalStars = progressManager.getTotalStarsEarned();
        int totalLevels = availableLevels.size();

        String progressText = "Total Levels: " + totalLevels + " | Total ★: " + totalStars;
        int textWidth = g2d.getFontMetrics().stringWidth(progressText);
        g2d.drawString(progressText, 320 - textWidth / 2, 115);
    }

    private void drawInstructions(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(255, 255, 255, 200));

        String instruction = "Click on a level to start | ESC to return to menu";
        int textWidth = g2d.getFontMetrics().stringWidth(instruction);
        g2d.drawString(instruction, 320 - textWidth / 2, 690);
    }

    private void drawLevelButtons(Graphics2D g2d) {
        for (LevelButton lb : levelButtons) {
            boolean isUnlocked = isLevelUnlocked(lb.levelId);
            LevelStats stats = progressManager.getLevelStats(lb.levelId);

            Color bgColor;
            if (!isUnlocked) {
                bgColor = LOCKED_GRAY;
            } else if (lb.isMouseOver) {
                bgColor = GRASS_GREEN;
            } else {
                bgColor = new Color(240, 240, 240);
            }

            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRoundRect(lb.x + 4, lb.y + 4, lb.width, lb.height, 15, 15);

            GradientPaint gradient = new GradientPaint(
                    lb.x, lb.y, bgColor,
                    lb.x, lb.y + lb.height, darken(bgColor, 0.8f));
            g2d.setPaint(gradient);
            g2d.fillRoundRect(lb.x, lb.y, lb.width, lb.height, 15, 15);

            if (lb.isMouseOver && isUnlocked) {
                float pulse = (float) Math.sin(animTick * 0.15) * 0.3f + 0.7f;
                g2d.setColor(new Color(255, 215, 0, (int) (pulse * 255)));
                g2d.drawRoundRect(lb.x - 1, lb.y - 1, lb.width + 2, lb.height + 2, 16, 16);
                g2d.setColor(GOLDEN_YELLOW);
                g2d.drawRoundRect(lb.x, lb.y, lb.width, lb.height, 15, 15);
            } else {
                g2d.setColor(DARK_BLUE);
                g2d.drawRoundRect(lb.x, lb.y, lb.width, lb.height, 15, 15);
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String levelText = "" + lb.levelId;
            int textWidth = g2d.getFontMetrics().stringWidth(levelText);

            if (isUnlocked) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(levelText, lb.x + lb.width / 2 - textWidth / 2 + 2, lb.y + 35);

                g2d.setColor(DARK_BLUE);
                g2d.drawString(levelText, lb.x + lb.width / 2 - textWidth / 2, lb.y + 33);

                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String displayName = lb.levelName;
                if (displayName.length() > 10) {
                    displayName = displayName.substring(0, 10) + "...";
                }
                int nameWidth = g2d.getFontMetrics().stringWidth(displayName);
                g2d.drawString(displayName, lb.x + lb.width / 2 - nameWidth / 2, lb.y + 50);

                drawDifficultyIndicator(g2d, lb.x + lb.width / 2, lb.y + 65, lb.difficulty);

            } else {
                drawLock(g2d, lb.x + lb.width / 2 - 15, lb.y + 20);

                g2d.setColor(new Color(100, 100, 100));
                g2d.drawString(levelText, lb.x + lb.width / 2 - textWidth / 2, lb.y + 33);
            }

            if (isUnlocked && stats != null) {
                drawMiniStars(g2d, lb.x + lb.width / 2 - 30, lb.y + lb.height - 25, stats.stars);
            } else if (isUnlocked) {
                drawMiniStars(g2d, lb.x + lb.width / 2 - 30, lb.y + lb.height - 25, 0);
            }

            if (isUnlocked && (stats == null || stats.timesCompleted == 0)) {
                float pulse = (float) Math.sin(animTick * 0.15) * 0.3f + 0.7f;
                g2d.setColor(new Color(255, 100, 100, (int) (255 * pulse)));
                g2d.fillOval(lb.x + lb.width - 25, lb.y + 5, 20, 20);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString("!", lb.x + lb.width - 19, lb.y + 19);
            }

            if (lb.isMouseOver && isUnlocked && stats != null && stats.highScore > 0) {
                drawScoreTooltip(g2d, lb.x + lb.width / 2, lb.y - 15, stats.highScore);
            }
        }
    }

    private void drawDifficultyIndicator(Graphics2D g2d, int x, int y, int difficulty) {
        String diffText = "";
        Color diffColor;

        switch (difficulty) {
            case 1:
                diffText = "Easy";
                diffColor = GRASS_GREEN;
                break;
            case 2:
                diffText = "Medium";
                diffColor = GOLDEN_YELLOW;
                break;
            case 3:
                diffText = "Hard";
                diffColor = new Color(231, 76, 60);
                break;
            default:
                diffText = "Normal";
                diffColor = SKY_BLUE;
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        int textWidth = g2d.getFontMetrics().stringWidth(diffText);

        g2d.setColor(diffColor);
        g2d.drawString(diffText, x - textWidth / 2, y);
    }

    private void drawScoreTooltip(Graphics2D g2d, int x, int y, int score) {
        String scoreText = "Best: " + score;
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int textWidth = g2d.getFontMetrics().stringWidth(scoreText);

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRoundRect(x - textWidth / 2 - 8, y - 18, textWidth + 16, 22, 8, 8);

        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawString(scoreText, x - textWidth / 2, y - 4);
    }

    private void drawLock(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRoundRect(x + 5, y + 15, 20, 25, 5, 5);

        g2d.drawArc(x + 7, y, 16, 20, 0, 180);
        g2d.drawArc(x + 6, y, 18, 20, 0, 180);

        g2d.setColor(new Color(120, 120, 120));
        g2d.fillOval(x + 12, y + 22, 6, 6);
        g2d.fillRect(x + 13, y + 26, 4, 8);
    }

    private void drawMiniStars(Graphics2D g2d, int x, int y, int count) {
        int starSize = 15;
        int spacing = 10;

        for (int i = 0; i < 3; i++) {
            int sx = x + i * (starSize + spacing);

            if (i < count) {
                drawMiniStar(g2d, sx, y, starSize, GOLDEN_YELLOW, true);
            } else {
                drawMiniStar(g2d, sx, y, starSize, new Color(200, 200, 200), false);
            }
        }
    }

    private void drawMiniStar(Graphics2D g2d, int x, int y, int size, Color color, boolean filled) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        double angle = -Math.PI / 2;
        double angleStep = Math.PI / 5;

        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? size / 2.0 : size / 4.0;
            xPoints[i] = (int) (x + Math.cos(angle) * radius);
            yPoints[i] = (int) (y + Math.sin(angle) * radius);
            angle += angleStep;
        }

        if (filled) {
            g2d.setColor(color);
            g2d.fillPolygon(xPoints, yPoints, 10);
        }

        g2d.setColor(DARK_BLUE);
        g2d.drawPolygon(xPoints, yPoints, 10);
    }

    private Color darken(Color color, float factor) {
        return new Color(
                Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0));
    }

    public boolean isLevelUnlocked(int level) {
        if (SessionManager.getInstance().isLoggedIn()) {
            int userId = SessionManager.getInstance().getUserId();
            return DatabaseManager.getInstance().isLevelUnlocked(userId, level);
        }
        return level == 1;
    }

    @Override
    public void mouseClicked(int x, int y) {
        // CLEANED: Removed debug spam
        if (bBack.getBounds().contains(x, y)) {
            GameStates.gameState = GameStates.MENU;
            game.repaint();
            return;
        }

        for (LevelButton lb : levelButtons) {
            if (lb.contains(x, y)) {
                if (isLevelUnlocked(lb.levelId)) {
                    progressManager.setCurrentLevel(lb.levelId);
                    game.getPlaying().loadDatabaseLevel(lb.levelId);
                    game.getPlaying().resetEverything();
                    GameStates.gameState = GameStates.PLAYING;
                    game.repaint();
                    return;
                }
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        bBack.setMouseOver(false);

        for (LevelButton lb : levelButtons) {
            lb.isMouseOver = false;
        }

        if (bBack.getBounds().contains(x, y)) {
            bBack.setMouseOver(true);
        } else {
            for (LevelButton lb : levelButtons) {
                if (lb.contains(x, y) && isLevelUnlocked(lb.levelId)) {
                    lb.isMouseOver = true;
                }
            }
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (bBack.getBounds().contains(x, y))
            bBack.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y) {
        bBack.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            setGameState(MENU);
        }
    }

    private static class LevelButton {
        int x, y, width, height;
        int levelId;
        String levelName;
        int difficulty;
        boolean isMouseOver;

        LevelButton(int x, int y, int width, int height, int levelId, String levelName) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.levelId = levelId;
            this.levelName = levelName;
            this.isMouseOver = false;

            Map<String, Object> levelData = DatabaseManager.getInstance().loadLevel(levelId);
            if (levelData != null) {
                this.difficulty = (int) levelData.get("difficulty");
            } else {
                this.difficulty = 1;
            }
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }
    }

    public void refreshLevels() {
        availableLevels = DatabaseManager.getInstance().getAllLevels();

        if (availableLevels.isEmpty()) {
            createDefaultLevels();
            availableLevels = DatabaseManager.getInstance().getAllLevels();
        }

        levelButtons.clear();
        initButtons();
    }
}