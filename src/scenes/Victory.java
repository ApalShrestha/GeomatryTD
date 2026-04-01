package scenes;

import static main.GameStates.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import javax.imageio.ImageIO;

import main.Game;
import managers.DatabaseManager;
import managers.LevelProgressionManager;
import ui.MyButton;

public class Victory extends GameScene implements SceneMethods {

    private MyButton bMenu, bNextLevel, bReplay;
    private int currentLevel;
    private int enemiesKilled;
    private int score;
    private int stars;
    private LevelProgressionManager progressManager;
    private ArrayList<Map<String, Object>> availableLevels;
    private BufferedImage kingHappyImg;
    
    private int animTick = 0;
    private float starScale = 0f;
    
    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
    private static final Color LIGHT_CREAM = new Color(255, 253, 208);
    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    private static final Color GRASS_GREEN = new Color(76, 175, 80);

    public Victory(Game game) {
        super(game);
        progressManager = new LevelProgressionManager();
        availableLevels = DatabaseManager.getInstance().getAllLevels();
        loadKingImage();
        initButtons();
    }
    
    private void loadKingImage() {
        try {
            File kingFile = new File("res/kingHappy1.png");
            if (kingFile.exists()) {
                kingHappyImg = ImageIO.read(kingFile);
                System.out.println("Successfully loaded kingHappy1.png");
            } else {
                System.out.println("Warning: Could not find kingHappy1.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading happy king image");
        }
    }

    private void initButtons() {
        int buttonWidth = 150;
        int buttonHeight = 40;
        int centerX = 320;
        int startY = 600;
        
        bMenu = new MyButton("Main Menu", centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
        bNextLevel = new MyButton("Next Level", centerX - buttonWidth / 2, startY + 50, buttonWidth, buttonHeight);
        bReplay = new MyButton("Replay Level", centerX - buttonWidth / 2, startY + 100, buttonWidth, buttonHeight);
    }

    public void setLevelStats(int level, int enemiesKilled, int score) {
        this.currentLevel = level;
        this.enemiesKilled = enemiesKilled;
        this.score = score;
        
        int livesRemaining = game.getPlaying().getActionBar().getLives();
        this.stars = progressManager.calculateStars(livesRemaining, 25);
        
        progressManager.completeLevel(level, stars, enemiesKilled, score);
        
        animTick = 0;
        starScale = 0f;
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        animTick++;
        starScale = Math.min(starScale + 0.05f, 1.0f);
        
        // Background gradient
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(100, 180, 255),
            0, 800, new Color(50, 120, 200)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, 640, 800);
        
        drawVictoryBanner(g2d);
        drawHappyKing(g2d);
        drawStatsPanel(g2d);
        drawStars(g2d);
        drawButtons(g2d);
    }
    
    private void drawVictoryBanner(Graphics2D g2d) {
        g2d.setColor(new Color(255, 215, 0, 200));
        g2d.fillRoundRect(120, 50, 400, 80, 20, 20);
        
        g2d.setColor(DARK_BLUE);
        g2d.drawRoundRect(120, 50, 400, 80, 20, 20);
        
        float textScale = 1.0f + (float)Math.sin(animTick * 0.1) * 0.1f;
        g2d.setFont(new Font("Arial", Font.BOLD, (int)(48 * textScale)));
        g2d.setColor(Color.WHITE);
        String victoryText = "VICTORY!";
        int textWidth = g2d.getFontMetrics().stringWidth(victoryText);
        g2d.drawString(victoryText, 320 - textWidth / 2 + 2, 95 + 2);
        
        g2d.setColor(DARK_BLUE);
        g2d.drawString(victoryText, 320 - textWidth / 2, 95);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(DARK_BLUE);
        
        String levelName = getCurrentLevelName();
        String levelText = levelName + " Complete";
        int levelWidth = g2d.getFontMetrics().stringWidth(levelText);
        g2d.drawString(levelText, 320 - levelWidth / 2, 120);
    }
    
    private void drawHappyKing(Graphics2D g2d) {
        if (kingHappyImg != null) {
            int kingWidth = 120;
            int kingHeight = (int) (kingHappyImg.getHeight() * ((float) kingWidth / kingHappyImg.getWidth()));
            int kingX = 50;
            int kingY = 170;
            
            // Add slight bounce animation
            float bounce = (float) Math.sin(animTick * 0.1) * 5;
            kingY += (int) bounce;
            
            g2d.drawImage(kingHappyImg, kingX, kingY, kingWidth, kingHeight, null);
            
            // Speech bubble with encouraging message
            drawSpeechBubble(g2d, 180, 180, 300, 80, "Well done, soldier!\nYou defended the kingdom!");
        }
    }
    
    private void drawSpeechBubble(Graphics2D g2d, int x, int y, int width, int height, String message) {
        // Bubble background
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillRoundRect(x, y, width, height, 15, 15);
        
        // Border
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawRoundRect(x, y, width, height, 15, 15);
        
        // Pointer to king
        int[] triX = {x, x - 15, x};
        int[] triY = {y + 20, y + 30, y + 40};
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillPolygon(triX, triY, 3);
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawPolygon(triX, triY, 3);
        
        // Message text
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(DARK_BLUE);
        String[] lines = message.split("\n");
        int lineY = y + 30;
        for (String line : lines) {
            int lineWidth = g2d.getFontMetrics().stringWidth(line);
            g2d.drawString(line, x + (width - lineWidth) / 2, lineY);
            lineY += 20;
        }
    }
    
    private String getCurrentLevelName() {
        Map<String, Object> levelData = DatabaseManager.getInstance().loadLevel(currentLevel);
        if (levelData != null) {
            return (String) levelData.get("level_name");
        }
        return "Level " + currentLevel;
    }
    
    private void drawStatsPanel(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(170, 280, 300, 150, 20, 20);
        
        g2d.setColor(DARK_BLUE);
        g2d.drawRoundRect(170, 280, 300, 150, 20, 20);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(DARK_BLUE);
        g2d.drawString("Level Statistics", 240, 310);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        
        g2d.setColor(new Color(231, 76, 60));
        g2d.drawString("Enemies Defeated:", 200, 345);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("" + enemiesKilled, 390, 345);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawString("Score:", 200, 375);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("" + score, 390, 375);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(GRASS_GREEN);
        g2d.drawString("Lives Remaining:", 200, 405);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        int lives = game.getPlaying().getActionBar().getLives();
        g2d.drawString("" + lives, 390, 405);
    }
    
    private void drawStars(Graphics2D g2d) {
        int starSize = (int)(60 * starScale);
        int startX = 320 - (starSize * 3 + 40) / 2;
        int y = 480;
        
        for (int i = 0; i < 3; i++) {
            int x = startX + i * (starSize + 20);
            
            if (i < stars) {
                drawStar(g2d, x, y, starSize, GOLDEN_YELLOW, true);
            } else {
                drawStar(g2d, x, y, starSize, new Color(200, 200, 200), false);
            }
        }
    }
    
    private void drawStar(Graphics2D g2d, int x, int y, int size, Color color, boolean filled) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        double angle = -Math.PI / 2;
        double angleStep = Math.PI / 5;
        
        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? size / 2.0 : size / 4.0;
            xPoints[i] = (int)(x + Math.cos(angle) * radius);
            yPoints[i] = (int)(y + Math.sin(angle) * radius);
            angle += angleStep;
        }
        
        if (filled) {
            g2d.setColor(color);
            g2d.fillPolygon(xPoints, yPoints, 10);
            
            g2d.setColor(new Color(255, 255, 150, 150));
            for (int i = 0; i < 10; i += 2) {
                int[] shineX = {xPoints[i], x, xPoints[(i + 2) % 10]};
                int[] shineY = {yPoints[i], y, yPoints[(i + 2) % 10]};
                g2d.fillPolygon(shineX, shineY, 3);
            }
        }
        
        g2d.setColor(DARK_BLUE);
        g2d.drawPolygon(xPoints, yPoints, 10);
    }
    
    private void drawButtons(Graphics2D g2d) {
        bMenu.draw(g2d);
        bReplay.draw(g2d);
        
        if (hasNextLevel()) {
            bNextLevel.draw(g2d);
        }
    }
    
    private boolean hasNextLevel() {
        for (Map<String, Object> level : availableLevels) {
            int levelId = (int) level.get("level_id");
            if (levelId == currentLevel + 1) {
                return progressManager.isLevelUnlocked(levelId);
            }
        }
        return false;
    }
    
    private int getNextLevelId() {
        for (Map<String, Object> level : availableLevels) {
            int levelId = (int) level.get("level_id");
            if (levelId == currentLevel + 1) {
                return levelId;
            }
        }
        return -1;
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (bMenu.getBounds().contains(x, y)) {
            setGameState(MENU);
        } else if (bNextLevel.getBounds().contains(x, y) && hasNextLevel()) {
            int nextLevelId = getNextLevelId();
            if (nextLevelId != -1) {
                progressManager.setCurrentLevel(nextLevelId);
                game.getPlaying().loadDatabaseLevel(nextLevelId);
                game.getPlaying().resetEverything();
                setGameState(PLAYING);
            }
        } else if (bReplay.getBounds().contains(x, y)) {
            game.getPlaying().loadDatabaseLevel(currentLevel);
            game.getPlaying().resetEverything();
            setGameState(PLAYING);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        bMenu.setMouseOver(false);
        bNextLevel.setMouseOver(false);
        bReplay.setMouseOver(false);

        if (bMenu.getBounds().contains(x, y))
            bMenu.setMouseOver(true);
        else if (bNextLevel.getBounds().contains(x, y) && hasNextLevel())
            bNextLevel.setMouseOver(true);
        else if (bReplay.getBounds().contains(x, y))
            bReplay.setMouseOver(true);
    }

    @Override
    public void mousePressed(int x, int y) {
        if (bMenu.getBounds().contains(x, y))
            bMenu.setMousePressed(true);
        else if (bNextLevel.getBounds().contains(x, y) && hasNextLevel())
            bNextLevel.setMousePressed(true);
        else if (bReplay.getBounds().contains(x, y))
            bReplay.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y) {
        bMenu.resetBooleans();
        bNextLevel.resetBooleans();
        bReplay.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            setGameState(MENU);
        }
    }
}