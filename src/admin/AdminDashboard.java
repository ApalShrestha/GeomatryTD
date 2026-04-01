package admin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

import managers.DatabaseManager;
import managers.SessionManager;
import ui.MyButton;

/**
 * Main admin dashboard showing system statistics and navigation
 */
public class AdminDashboard {

    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
    private static final Color GRASS_GREEN = new Color(76, 175, 80);

    private MyButton bMapEditor, bLeaderboard, bPlayerManager, bLevelManager, bLogout;
    private ArrayList<Map<String, Object>> players;
    private ArrayList<Map<String, Object>> levels;
    private ArrayList<Map<String, Object>> highScores;

    public AdminDashboard() {
        initButtons();
        loadStats();
    }

    private void initButtons() {
        int w = 200;
        int h = 50;
        int centerX = 320;
        int startY = 150;
        int spacing = 70;

        bMapEditor = new MyButton("Map Editor", centerX - w / 2, startY, w, h);
        bLeaderboard = new MyButton("View Leaderboard", centerX - w / 2, startY + spacing, w, h);
        bPlayerManager = new MyButton("Manage Players", centerX - w / 2, startY + spacing * 2, w, h);
        bLevelManager = new MyButton("Manage Levels", centerX - w / 2, startY + spacing * 3, w, h);
        bLogout = new MyButton("Logout", centerX - w / 2, 700, w, h);
    }

    private void loadStats() {
        players = DatabaseManager.getInstance().getAllUsers();
        levels = DatabaseManager.getInstance().getAllLevels();
        highScores = DatabaseManager.getInstance().getAllHighScores();
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background gradient
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(50, 50, 70),
                0, 800, new Color(30, 30, 50));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, 640, 800);

        // Title
        drawTitle(g2d);

        // Statistics panel
        drawStatsPanel(g2d);

        // Navigation buttons
        bMapEditor.draw(g);
        bLeaderboard.draw(g);
        bPlayerManager.draw(g);
        bLevelManager.draw(g);
        bLogout.draw(g);
    }

    private void drawTitle(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 38));
        String title = "ADMIN DASHBOARD";

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, 320 - titleWidth / 2 + 2, 52);

        // Main title with gradient
        GradientPaint titleGradient = new GradientPaint(
                0, 30, new Color(255, 100, 100),
                0, 60, new Color(255, 50, 50));
        g2d.setPaint(titleGradient);
        g2d.drawString(title, 320 - titleWidth / 2, 50);

        // Welcome message
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        String welcome = "Welcome, " + SessionManager.getInstance().getUsername();
        int welcomeWidth = g2d.getFontMetrics().stringWidth(welcome);
        g2d.drawString(welcome, 320 - welcomeWidth / 2, 80);
    }

    private void drawStatsPanel(Graphics2D g2d) {
        int panelX = 120;
        int panelY = 420;
        int panelW = 400;
        int panelH = 240;

        // Background + Border
        g2d.setColor(new Color(60, 60, 90, 200));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(GOLDEN_YELLOW);
        String title = "SYSTEM STATISTICS";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, panelX + (panelW - fm.stringWidth(title)) / 2, panelY + 35);

        // Stats font
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        fm = g2d.getFontMetrics(); // refresh metrics

        int leftX = panelX + 30;
        int rightX = panelX + panelW - 30;
        int y = panelY + 80;
        int lineH = 35;

        // Total Players
        g2d.setColor(SKY_BLUE);
        g2d.drawString("Total Players:", leftX, y);
        String pCount = String.valueOf(Math.max(0, players.size() - 1));
        g2d.setColor(Color.WHITE);
        g2d.drawString(pCount, rightX - fm.stringWidth(pCount), y);
        y += lineH;

        // Total Levels
        g2d.setColor(GRASS_GREEN);
        g2d.drawString("Total Levels:", leftX, y);
        String lCount = String.valueOf(levels.size());
        g2d.setColor(Color.WHITE);
        g2d.drawString(lCount, rightX - fm.stringWidth(lCount), y);
        y += lineH;

        // Total Scores
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawString("Total Scores:", leftX, y);
        String sCount = String.valueOf(highScores.size());
        g2d.setColor(Color.WHITE);
        g2d.drawString(sCount, rightX - fm.stringWidth(sCount), y);
        y += lineH;

        // Active Players
        int active = 0;
        for (Map<String, Object> p : players) {
            if (Boolean.TRUE.equals(p.get("is_active")) &&
                    Boolean.FALSE.equals(p.get("is_admin"))) {
                active++;
            }
        }
        g2d.setColor(GRASS_GREEN);
        g2d.drawString("Active Players:", leftX, y);
        String aCount = String.valueOf(active);
        g2d.setColor(Color.WHITE);
        g2d.drawString(aCount, rightX - fm.stringWidth(aCount), y);
        y += lineH;

        // Database Status - NOW PERFECTLY ALIGNED
        g2d.setColor(GRASS_GREEN);
        g2d.drawString("Database:", leftX, y);
        g2d.setColor(Color.WHITE);
        String dbStatus = "Connected";
        g2d.drawString(dbStatus, rightX - fm.stringWidth(dbStatus), y);
    }

    // ADD THESE EXACT METHODS TO YOUR AdminDashboard CLASS!!!

    public MyButton getMapEditorButton() {
        return bMapEditor;
    }

    public MyButton getLeaderboardButton() {
        return bLeaderboard;
    }

    public MyButton getPlayerManagerButton() {
        return bPlayerManager;
    }

    public MyButton getLevelManagerButton() {
        return bLevelManager;
    }

    public MyButton getLogoutButton() {
        return bLogout;
    }

    public void refreshStats() {
        loadStats();
    }
}
