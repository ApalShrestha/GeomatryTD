package admin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

import managers.DatabaseManager;
import ui.MyButton;

/**
 * Admin interface for managing game levels
 */
public class LevelManager {
    
    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
    private static final Color GRASS_GREEN = new Color(76, 175, 80);
    private static final Color RED_DELETE = new Color(231, 76, 60);
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    
    private ArrayList<Map<String, Object>> levels;
    private int scrollOffset;
    private int selectedLevelId;
    private MyButton bBack, bRefresh, bCreateNew;
    
    // Callback for edit action
    private LevelEditCallback editCallback;
    
    public interface LevelEditCallback {
        void onEditLevel(int levelId);
    }
    
    public LevelManager() {
        initButtons();
        loadLevels();
    }
    
    private void initButtons() {
        bBack = new MyButton("Back", 20, 700, 100, 40);
        bRefresh = new MyButton("Refresh", 270, 700, 100, 40);
        bCreateNew = new MyButton("Create New", 520, 700, 100, 40);
    }
    
    private void loadLevels() {
        levels = DatabaseManager.getInstance().getAllLevels();
    }
    
    public void render(Graphics2D g2d) {
        // Title
        
        g2d.setFont(new Font("Arial", Font.BOLD, 38));
        String title = "LEVEL MANAGEMENT";
        
        g2d.setColor(new Color(0, 0, 0, 100));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, 320 - titleWidth/2 + 2, 52);
        
        GradientPaint titleGradient = new GradientPaint(
            0, 30, new Color(255, 100, 100),
            0, 60, new Color(255, 50, 50)
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, 320 - titleWidth/2, 50);
        
        // Levels panel
        g2d.setColor(new Color(40, 40, 60, 230));
        g2d.fillRoundRect(50, 110, 540, 560, 15, 15);
        
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawRoundRect(50, 110, 540, 560, 15, 15);
        
        // Column headers
        drawHeaders(g2d);
        
        // Levels list
        drawLevels(g2d);
        
        // Instructions
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.setColor(new Color(200, 200, 200));
        String inst = "Click 'Edit' to modify | 'Del' to delete | 'Toggle' to activate/deactivate | UP/DOWN to scroll";
        int instWidth = g2d.getFontMetrics().stringWidth(inst);
        g2d.drawString(inst, 320 - instWidth/2, 685);
        
        // Buttons
        bBack.draw((Graphics) g2d);
        bRefresh.draw((Graphics) g2d);
        bCreateNew.draw((Graphics) g2d);
    }
    
    private void drawHeaders(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        
        g2d.drawString("ID", 70, 140);
        g2d.drawString("Level Name", 110, 140);
        g2d.drawString("Difficulty", 280, 140);
        g2d.drawString("Status", 370, 140);
        g2d.drawString("Actions", 460, 140);
        
        // Header line
        g2d.setColor(new Color(255, 215, 0, 100));
        g2d.fillRect(60, 148, 520, 2);
    }
    
    private void drawLevels(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        int y = 170;
        int maxDisplay = 18;
        
        for (int i = scrollOffset; i < Math.min(levels.size(), scrollOffset + maxDisplay); i++) {
            Map<String, Object> level = levels.get(i);
            int levelId = (int) level.get("level_id");
            
            // Alternate row colors
            if ((i - scrollOffset) % 2 == 0) {
                g2d.setColor(new Color(50, 50, 70, 100));
                g2d.fillRect(60, y - 15, 520, 25);
            }
            
            // Highlight selected
            if (levelId == selectedLevelId) {
                g2d.setColor(new Color(100, 149, 237, 100));
                g2d.fillRect(60, y - 15, 520, 25);
            }
            
            // Level data
            g2d.setColor(Color.WHITE);
            g2d.drawString("" + levelId, 70, y);
            
            String levelName = (String) level.get("level_name");
            if (levelName.length() > 16) levelName = levelName.substring(0, 16) + "...";
            g2d.drawString(levelName, 110, y);
            
            // Difficulty with color
            int difficulty = (int) level.get("difficulty");
            g2d.setColor(difficulty == 1 ? GRASS_GREEN : 
                        difficulty == 2 ? GOLDEN_YELLOW : RED_DELETE);
            g2d.drawString("Level " + difficulty, 280, y);
            
            // Status
            boolean isActive = (boolean) level.get("is_active");
            g2d.setColor(isActive ? GRASS_GREEN : RED_DELETE);
            g2d.drawString(isActive ? "Active" : "Inactive", 370, y);
            
            // Action buttons
            drawActionButtons(g2d, y);
            
            y += 28;
        }
    }
    
    private void drawActionButtons(Graphics2D g2d, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        
        // Edit button
        g2d.setColor(SKY_BLUE);
        g2d.fillRoundRect(460, y - 12, 35, 20, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Edit", 467, y + 2);
        
        // Toggle button
        g2d.setColor(GOLDEN_YELLOW);
        g2d.fillRoundRect(500, y - 12, 35, 20, 5, 5);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Tog", 505, y + 2);
        
        // Delete button
        g2d.setColor(RED_DELETE);
        g2d.fillRoundRect(540, y - 12, 30, 20, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Del", 545, y + 2);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
    }
    
    public boolean handleClick(int x, int y) {
        // Check button clicks
        if (bRefresh.getBounds().contains(x, y)) {
            refresh();
            return true;
        }
        
        if (bCreateNew.getBounds().contains(x, y)) {
            if (editCallback != null) {
                editCallback.onEditLevel(-1); // -1 means create new
            }
            return true;
        }
        
        // Check level action buttons
        int clickY = 170;
        int rowHeight = 28;
        
        for (int i = scrollOffset; i < Math.min(levels.size(), scrollOffset + 18); i++) {
            Map<String, Object> level = levels.get(i);
            int levelId = (int) level.get("level_id");
            
            // Edit button clicked
            if (x >= 460 && x <= 495 && y >= clickY - 12 && y <= clickY + 8) {
                selectedLevelId = levelId;
                if (editCallback != null) {
                    editCallback.onEditLevel(levelId);
                }
                return true;
            }
            
            // Toggle button clicked
            if (x >= 500 && x <= 535 && y >= clickY - 12 && y <= clickY + 8) {
                toggleLevelActive(levelId);
                return true;
            }
            
            // Delete button clicked
            if (x >= 540 && x <= 570 && y >= clickY - 12 && y <= clickY + 8) {
                if (DatabaseManager.getInstance().deleteLevel(levelId)) {
                    System.out.println("Level deleted: " + levelId);
                    loadLevels();
                }
                return true;
            }
            
            clickY += rowHeight;
        }
        
        return false;
    }
    
    private void toggleLevelActive(int levelId) {
        // This would need to be implemented in DatabaseManager
        System.out.println("Toggle level active status: " + levelId);
        // For now, just refresh
        refresh();
    }
    
    public void scrollUp() {
        scrollOffset = Math.max(0, scrollOffset - 1);
    }
    
    public void scrollDown() {
        int maxScroll = Math.max(0, levels.size() - 18);
        scrollOffset = Math.min(maxScroll, scrollOffset + 1);
    }
    
    public void refresh() {
        loadLevels();
        scrollOffset = 0;
    }
    
    public void setEditCallback(LevelEditCallback callback) {
        this.editCallback = callback;
    }
    
    public MyButton getBackButton() { return bBack; }
    public MyButton getRefreshButton() { return bRefresh; }
    public MyButton getCreateNewButton() { return bCreateNew; }
    
    public void updateHover(int x, int y) {
        bBack.setMouseOver(bBack.getBounds().contains(x, y));
        bRefresh.setMouseOver(bRefresh.getBounds().contains(x, y));
        bCreateNew.setMouseOver(bCreateNew.getBounds().contains(x, y));
    }
}