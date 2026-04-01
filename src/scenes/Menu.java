package scenes;

import static main.GameStates.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import main.Game;
import managers.SessionManager;
import ui.MyButton;

public class Menu extends GameScene implements SceneMethods {

    private MyButton bPlaying, bSettings, bLogout, bQuit;
    private int animTick = 0;
    private BufferedImage backgroundImg;

    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    private static final Color GRASS_GREEN = new Color(76, 175, 80);

    public Menu(Game game) {
        super(game);
        loadBackground();
        initButtons();
    }
    
    private void loadBackground() {
        try {
            File bgFile = new File("res/GeoMatryTD.png");
            if (bgFile.exists()) {
                backgroundImg = ImageIO.read(bgFile);
            }
        } catch (Exception e) {
            // Silent fallback - will use gradient instead
        }
    }

    public void initButtons() {
        int w = 180;
        int h = 50;
        int x = 640 / 2 - w / 2;
        int y = 350;
        int yOffset = 70;

        bPlaying = new MyButton("Play", x, y, w, h);
        bSettings = new MyButton("Settings", x, y + yOffset, w, h);
        bLogout = new MyButton("Logout", x, y + yOffset * 2, w, h);
        
        if (SessionManager.getInstance().isLoggedIn()) {
            bQuit = new MyButton("Quit", x, y + yOffset * 3, w, h);
        } else {
            bQuit = new MyButton("Quit", x, y + yOffset * 2, w, h);
        }
    }

    public void update() {
        animTick++;
    }

    @Override
    public void render(Graphics g) {
        // CLEANED: Removed animTick++ duplicate
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (backgroundImg != null) {
            g2d.drawImage(backgroundImg, 0, 0, 640, 800, null);
        } else {
            GradientPaint bgGradient = new GradientPaint(
                    0, 0, SKY_BLUE,
                    0, 800, GRASS_GREEN);
            g2d.setPaint(bgGradient);
            g2d.fillRect(0, 0, 640, 800);
            drawClouds(g2d);
        }

        drawTitle(g2d);

        bPlaying.draw(g);
        bSettings.draw(g);

        if (SessionManager.getInstance().isLoggedIn()) {
            bLogout.draw(g);
            bQuit.setBounds(640/2 - 90, 350 + 70 * 3, 180, 50);
            bQuit.draw(g);
        } else {
            bQuit.setBounds(640/2 - 90, 350 + 70 * 2, 180, 50);
            bQuit.draw(g);
        }

        if (backgroundImg == null) {
            drawDecorations(g2d);
        }
    }

    private void drawTitle(Graphics2D g2d) {
        float titleScale = 1.0f + (float) Math.sin(animTick * 0.05) * 0.05f;
        int fontSize = (int) (72 * titleScale);

        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));

        g2d.setColor(new Color(0, 0, 0, 80));
        String title = "";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, 320 - titleWidth / 2 + 4, 154);

        GradientPaint titleGradient = new GradientPaint(
                0, 100, GOLDEN_YELLOW,
                0, 180, new Color(255, 180, 0));
        g2d.setPaint(titleGradient);
        g2d.drawString(title, 320 - titleWidth / 2, 150);

        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(Color.WHITE);
        String subtitle = "";
        int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, 320 - subWidth / 2 + 2, 192);

        g2d.setColor(DARK_BLUE);
        g2d.drawString(subtitle, 320 - subWidth / 2, 190);
    }

    private void drawClouds(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 150));

        int cloud1X = (animTick * 2) % 700 - 100;
        int cloud2X = (animTick * 3) % 800 - 150;

        g2d.fillOval(cloud1X, 50, 80, 40);
        g2d.fillOval(cloud1X + 30, 40, 60, 40);
        g2d.fillOval(cloud1X + 60, 50, 70, 40);

        g2d.fillOval(cloud2X, 120, 100, 50);
        g2d.fillOval(cloud2X + 40, 110, 80, 50);
        g2d.fillOval(cloud2X + 80, 120, 90, 50);
    }

    private void drawDecorations(Graphics2D g2d) {
        g2d.setColor(new Color(60, 140, 60));
        for (int i = 0; i < 640; i += 20) {
            int offset = (int) (Math.sin((animTick + i) * 0.1) * 5);
            g2d.fillRect(i, 750 + offset, 3, 50);
            g2d.fillRect(i + 10, 760 + offset, 3, 40);
        }
    }

    public void keyPressed(KeyEvent e) {
        // ESC handled globally
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (SessionManager.getInstance().isLoggedIn()) {
            bQuit.setBounds(640/2 - 90, 350 + 70 * 3, 180, 50);
        } else {
            bQuit.setBounds(640/2 - 90, 350 + 70 * 2, 180, 50);
        }
        
        if (bPlaying.getBounds().contains(x, y)) {
            setGameState(STORY);
        } else if (bSettings.getBounds().contains(x, y)) {
            setGameState(SETTINGS);
        } else if (bLogout.getBounds().contains(x, y) && SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().logout();
            bQuit.setBounds(640/2 - 90, 350 + 70 * 2, 180, 50);
            game.repaint();
        } else if (bQuit.getBounds().contains(x, y)) {
            System.exit(0);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        bPlaying.setMouseOver(false);
        bSettings.setMouseOver(false);
        bLogout.setMouseOver(false);
        bQuit.setMouseOver(false);

        if (bPlaying.getBounds().contains(x, y)) {
            bPlaying.setMouseOver(true);
        } else if (bSettings.getBounds().contains(x, y)) {
            bSettings.setMouseOver(true);
        } else if (SessionManager.getInstance().isLoggedIn() && bLogout.getBounds().contains(x, y)) {
            bLogout.setMouseOver(true);
        } else if (bQuit.getBounds().contains(x, y)) {
            bQuit.setMouseOver(true);
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (bPlaying.getBounds().contains(x, y)) {
            bPlaying.setMousePressed(true);
        } else if (bSettings.getBounds().contains(x, y)) {
            bSettings.setMousePressed(true);
        } else if (SessionManager.getInstance().isLoggedIn() && bLogout.getBounds().contains(x, y)) {
            bLogout.setMousePressed(true);
        } else if (bQuit.getBounds().contains(x, y)) {
            bQuit.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        bPlaying.resetBooleans();
        bSettings.resetBooleans();
        bLogout.resetBooleans();
        bQuit.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {
    }
}