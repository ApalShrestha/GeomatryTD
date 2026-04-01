package admin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

import managers.DatabaseManager;
import ui.MyButton;

/**
 * Admin view for leaderboard and high scores
 */
public class LeaderboardViewer {
    
    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
    private static final Color GRASS_GREEN = new Color(76, 175, 80);
    
    private ArrayList<Map<String, Object>> highScores;
    private int scrollOffset;
    private MyButton bBack, bRefresh;
    
    public LeaderboardViewer() {
        initButtons();
        loadScores();
    }
    
    private void initButtons() {
        bBack = new MyButton("Back", 20, 700, 100, 40);
        bRefresh = new MyButton("Refresh", 520, 700, 100, 40);
    }
    
    private void loadScores() {
        highScores = DatabaseManager.getInstance().getAllHighScores();
    }
    
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 38));
        String title = "LEADERBOARD";
        
        g2d.setColor(new Color(0, 0, 0, 100));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, 320 - titleWidth/2 + 2, 52);
        
        GradientPaint titleGradient = new GradientPaint(
            0, 30, new Color(255, 100, 100),
            0, 60, new Color(255, 50, 50)
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, 320 - titleWidth/2, 50);
        
        // Leaderboard panel
        g2d.setColor(new Color(40, 40, 60, 230));
        g2d.fillRoundRect(50, 110, 540, 560, 15, 15);
        
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawRoundRect(50, 110, 540, 560, 15, 15);
        
        // Column headers
        drawHeaders(g2d);
        
        // Scores list
        drawScores(g2d);
        
        // Instructions
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.setColor(new Color(200, 200, 200));
        String inst = "UP/DOWN arrows to scroll | Top 100 scores displayed";
        int instWidth = g2d.getFontMetrics().stringWidth(inst);
        g2d.drawString(inst, 320 - instWidth/2, 685);
        
        // Buttons
        bBack.draw(g);
        bRefresh.draw(g);
    }
    
    private void drawHeaders(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        
        g2d.drawString("Rank", 70, 140);
        g2d.drawString("Player", 120, 140);
        g2d.drawString("Level", 240, 140);
        g2d.drawString("Score", 300, 140);
        g2d.drawString("Stars", 370, 140);
        g2d.drawString("Kills", 430, 140);
        g2d.drawString("Date", 490, 140);
        
        // Header line
        g2d.setColor(new Color(255, 215, 0, 100));
        g2d.fillRect(60, 148, 520, 2);
    }
    
    private void drawScores(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int y = 170;
        int maxDisplay = 18;
        
        for (int i = scrollOffset; i < Math.min(highScores.size(), scrollOffset + maxDisplay); i++) {
            Map<String, Object> score = highScores.get(i);
            
            // Alternate row colors
            if ((i - scrollOffset) % 2 == 0) {
                g2d.setColor(new Color(50, 50, 70, 100));
                g2d.fillRect(60, y - 15, 520, 25);
            }
            
            // Rank with medals for top 3
            g2d.setColor(GOLDEN_YELLOW);
            String rank = (i + 1) + ".";
            if (i == 0) rank = "1st";
            else if (i == 1) rank = "2nd";
            else if (i == 2) rank = "3rd";
            g2d.drawString(rank, 70, y);
            
            // Player data
            g2d.setColor(Color.WHITE);
            String username = (String) score.get("username");
            if (username.length() > 10) username = username.substring(0, 10) + "...";
            g2d.drawString(username, 120, y);
            
            g2d.drawString("" + score.get("level_id"), 250, y);
            g2d.drawString("" + score.get("score"), 300, y);
            
            // Stars
            int stars = (int) score.get("stars");
            g2d.setColor(GOLDEN_YELLOW);
            String starStr = "";
            for (int s = 0; s < stars; s++) starStr += "*";
            g2d.drawString(starStr, 370, y);
            
            // Kills
            g2d.setColor(Color.WHITE);
            g2d.drawString("" + score.get("enemies_killed"), 440, y);
            
            // Date
            String date = (String) score.get("achieved_at");
            g2d.setColor(new Color(180, 180, 180));
            g2d.drawString(date.substring(0, 10), 490, y);
            
            y += 28;
        }
    }
    
    public void scrollUp() {
        scrollOffset = Math.max(0, scrollOffset - 1);
    }
    
    public void scrollDown() {
        int maxScroll = Math.max(0, highScores.size() - 18);
        scrollOffset = Math.min(maxScroll, scrollOffset + 1);
    }
    
    public void refresh() {
        loadScores();
        scrollOffset = 0;
    }
    
    public MyButton getBackButton() { return bBack; }
    public MyButton getRefreshButton() { return bRefresh; }
    
    public boolean handleClick(int x, int y) {
        if (bRefresh.getBounds().contains(x, y)) {
            refresh();
            return true;
        }
        return false;
    }
    
    public void updateHover(int x, int y) {
        bBack.setMouseOver(bBack.getBounds().contains(x, y));
        bRefresh.setMouseOver(bRefresh.getBounds().contains(x, y));
    }
}
