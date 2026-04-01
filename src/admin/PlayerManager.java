package admin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

import managers.DatabaseManager;
import ui.MyButton;

/**
 * Admin interface for managing players
 */
public class PlayerManager {
    
    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
    private static final Color GRASS_GREEN = new Color(76, 175, 80);
    private static final Color RED_DELETE = new Color(231, 76, 60);
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    
    private ArrayList<Map<String, Object>> players;
    private int scrollOffset;
    private int selectedPlayerId;
    private MyButton bBack, bRefresh;
    
    public PlayerManager() {
        initButtons();
        loadPlayers();
    }
    
    private void initButtons() {
        bBack = new MyButton("Back", 20, 700, 100, 40);
        bRefresh = new MyButton("Refresh", 520, 700, 100, 40);
    }
    
    private void loadPlayers() {
        players = DatabaseManager.getInstance().getAllUsers();
    }
    
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 38));
        String title = "PLAYER MANAGEMENT";
        
        g2d.setColor(new Color(0, 0, 0, 100));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, 320 - titleWidth/2 + 2, 52);
        
        GradientPaint titleGradient = new GradientPaint(
            0, 30, new Color(255, 100, 100),
            0, 60, new Color(255, 50, 50)
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, 320 - titleWidth/2, 50);
        
        // Players panel
        g2d.setColor(new Color(40, 40, 60, 230));
        g2d.fillRoundRect(50, 110, 540, 560, 15, 15);
        
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawRoundRect(50, 110, 540, 560, 15, 15);
        
        // Column headers
        drawHeaders(g2d);
        
        // Players list
        drawPlayers(g2d);
        
        // Instructions
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.setColor(new Color(200, 200, 200));
        String inst = "Click 'Delete' to remove player | 'Toggle' to activate/deactivate | UP/DOWN to scroll";
        int instWidth = g2d.getFontMetrics().stringWidth(inst);
        g2d.drawString(inst, 320 - instWidth/2, 685);
        
        // Buttons
        bBack.draw(g);
        bRefresh.draw(g);
    }
    
    private void drawHeaders(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        
        g2d.drawString("ID", 70, 140);
        g2d.drawString("Username", 110, 140);
        g2d.drawString("Email", 230, 140);
        g2d.drawString("Status", 380, 140);
        g2d.drawString("Actions", 470, 140);
        
        // Header line
        g2d.setColor(new Color(255, 215, 0, 100));
        g2d.fillRect(60, 148, 520, 2);
    }
    
    private void drawPlayers(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        int y = 170;
        int maxDisplay = 18;
        
        for (int i = scrollOffset; i < Math.min(players.size(), scrollOffset + maxDisplay); i++) {
            Map<String, Object> player = players.get(i);
            
            // Skip admin
            if ((boolean) player.get("is_admin")) continue;
            
            int playerId = (int) player.get("id");
            
            // Alternate row colors
            if ((i - scrollOffset) % 2 == 0) {
                g2d.setColor(new Color(50, 50, 70, 100));
                g2d.fillRect(60, y - 15, 520, 25);
            }
            
            // Highlight selected
            if (playerId == selectedPlayerId) {
                g2d.setColor(new Color(100, 149, 237, 100));
                g2d.fillRect(60, y - 15, 520, 25);
            }
            
            // Player data
            g2d.setColor(Color.WHITE);
            g2d.drawString("" + playerId, 70, y);
            
            String username = (String) player.get("username");
            if (username.length() > 12) username = username.substring(0, 12) + "...";
            g2d.drawString(username, 110, y);
            
            String email = (String) player.get("email");
            if (email != null && email.length() > 15) email = email.substring(0, 15) + "...";
            g2d.drawString(email != null ? email : "N/A", 230, y);
            
            // Status
            boolean isActive = (boolean) player.get("is_active");
            g2d.setColor(isActive ? GRASS_GREEN : RED_DELETE);
            g2d.drawString(isActive ? "Active" : "Inactive", 380, y);
            
            // Action buttons
            drawActionButtons(g2d, y, playerId);
            
            y += 28;
        }
    }
    
    private void drawActionButtons(Graphics2D g2d, int y, int playerId) {
        // Toggle button
        g2d.setColor(SKY_BLUE);
        g2d.fillRoundRect(470, y - 12, 45, 20, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("Toggle", 473, y + 2);
        
        // Delete button
        g2d.setColor(RED_DELETE);
        g2d.fillRoundRect(520, y - 12, 40, 20, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Del", 528, y + 2);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
    }
    
    public boolean handleClick(int x, int y) {
        // Check refresh button
        if (bRefresh.getBounds().contains(x, y)) {
            refresh();
            return true;
        }
        
        // Check player action buttons
        int clickY = 170;
        int rowHeight = 28;
        
        for (int i = scrollOffset; i < Math.min(players.size(), scrollOffset + 18); i++) {
            Map<String, Object> player = players.get(i);
            
            if ((boolean) player.get("is_admin")) continue;
            
            int playerId = (int) player.get("id");
            
            // Toggle button clicked
            if (x >= 470 && x <= 515 && y >= clickY - 12 && y <= clickY + 8) {
                if (DatabaseManager.getInstance().toggleUserActive(playerId)) {
                    System.out.println("Player status toggled: " + player.get("username"));
                    loadPlayers();
                }
                return true;
            }
            
            // Delete button clicked
            if (x >= 520 && x <= 560 && y >= clickY - 12 && y <= clickY + 8) {
                if (DatabaseManager.getInstance().deleteUser(playerId)) {
                    System.out.println("Player deleted: " + player.get("username"));
                    loadPlayers();
                }
                return true;
            }
            
            clickY += rowHeight;
        }
        
        return false;
    }
    
    public void scrollUp() {
        scrollOffset = Math.max(0, scrollOffset - 1);
    }
    
    public void scrollDown() {
        int maxScroll = Math.max(0, players.size() - 18);
        scrollOffset = Math.min(maxScroll, scrollOffset + 1);
    }
    
    public void refresh() {
        loadPlayers();
        scrollOffset = 0;
    }
    
    public MyButton getBackButton() { return bBack; }
    public MyButton getRefreshButton() { return bRefresh; }
    
    public void updateHover(int x, int y) {
        bBack.setMouseOver(bBack.getBounds().contains(x, y));
        bRefresh.setMouseOver(bRefresh.getBounds().contains(x, y));
    }
}