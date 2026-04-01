package admin;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import helpz.LoadSave;
import managers.TileManager;
import objects.Tile;
import ui.MyButton;

/**
 * Admin Toolbar for Map Editor - Provides tile selection for level creation/editing
 * All buttons are now contained within the toolbar area (710px to 800px)
 */
public class AdminToolbar {
    
    // Admin color scheme with transparency
    private static final Color DARK_BLUE = new Color(25, 55, 109, 200);
    private static final Color SKY_BLUE = new Color(135, 206, 235, 180);
    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0, 180);
    private static final Color GRASS_GREEN = new Color(76, 175, 80, 180);
    private static final Color LIGHT_CREAM = new Color(240, 240, 240, 150);
    
    private int x, y, width, height;
    private MapEditor mapEditor;
    private MyButton bValidate, bSave, bCancel;
    private MyButton bPathStart, bPathEnd;
    private BufferedImage pathStart, pathEnd;
    private Tile selectedTile;

    private Map<MyButton, ArrayList<Tile>> map = new HashMap<MyButton, ArrayList<Tile>>();

    private MyButton bGrass, bWater, bRoadS, bRoadC, bWaterC, bWaterB, bWaterI;
    private MyButton currentButton;
    private int currentIndex = 0;
    
    private int animTick = 0;

    public AdminToolbar(int x, int y, int width, int height, MapEditor mapEditor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.mapEditor = mapEditor;
        initPathImgs();
        initButtons();
    }

    private void initPathImgs() {
        pathStart = LoadSave.getSpriteAtlas().getSubimage(7 * 32, 2 * 32, 32, 32);
        pathEnd = LoadSave.getSpriteAtlas().getSubimage(8 * 32, 2 * 32, 32, 32);
    }

    private void initButtons() {
        // All buttons are positioned relative to toolbar Y (710)
        
        // Tile selection buttons - positioned in toolbar area
        int w = 50;
        int h = 50;
        int xStart = 10;  // Start from left edge
        int yStart = y + 5; // Position within toolbar (710 + 5)
        int xOffset = 55; // Spacing between buttons
        
        // Create basic tile buttons
        bGrass = new MyButton("Grass", xStart, yStart, w, h, 0);
        bWater = new MyButton("Water", xStart + xOffset, yStart, w, h, 1);
        
        // Create map buttons
        bRoadS = new MyButton("", xStart + xOffset * 2, yStart, w, h, 2);
        bRoadC = new MyButton("", xStart + xOffset * 3, yStart, w, h, 3);
        bWaterC = new MyButton("", xStart + xOffset * 4, yStart, w, h, 4);
        bWaterB = new MyButton("", xStart + xOffset * 5, yStart, w, h, 5);
        bWaterI = new MyButton("", xStart + xOffset * 6, yStart, w, h, 6);
        
        // Path point buttons - second row within toolbar
        bPathStart = new MyButton("PathStart", xStart, yStart + xOffset, w, h, 7);
        bPathEnd = new MyButton("PathEnd", xStart + xOffset, yStart + xOffset, w, h, 8);
        
        // Action buttons - positioned at right side of toolbar
        bValidate = new MyButton("Validate", 220, y + 55, 90, 35);
        bSave = new MyButton("Save to DB", 420, y + 55, 90, 35);
        bCancel = new MyButton("Cancel", 320, y + 55, 90, 35);
        
        // Populate the map with tile lists
        map.put(bRoadS, getTileManager().getRoadsS());
        map.put(bRoadC, getTileManager().getRoadsC());
        map.put(bWaterC, getTileManager().getCorners());
        map.put(bWaterB, getTileManager().getBeaches());
        map.put(bWaterI, getTileManager().getIslands());
        
        // Set default selected tile
        selectedTile = getTileManager().getTile(bGrass.getId());
        mapEditor.setSelectedTile(selectedTile);
    }

    private TileManager getTileManager() {
        return mapEditor.getGame().getTileManager();
    }

    public void rotateSprite() {
        if (currentButton != null && map.containsKey(currentButton)) {
            currentIndex++;
            if (currentIndex >= map.get(currentButton).size())
                currentIndex = 0;
            selectedTile = map.get(currentButton).get(currentIndex);
            mapEditor.setSelectedTile(selectedTile);
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        animTick++;

        // Semi-transparent toolbar background
        g2d.setColor(new Color(40, 40, 60, 180));
        g2d.fillRect(x, y, width, height);
        
        // Top border
        g2d.setColor(new Color(255, 215, 0, 200));
        g2d.fillRect(x, y, width, 2);

        // Buttons
        drawButtons(g2d);
    }

    private void drawButtons(Graphics2D g2d) {
        // Draw basic tile buttons
        drawTileButton(g2d, bGrass, "Grass");
        drawTileButton(g2d, bWater, "Water");
        
        // Draw map buttons (road corners, beaches, etc.)
        drawMapButtons(g2d);
        
        // Draw path point buttons
        drawPathButton(g2d, bPathStart, pathStart, "Start");
        drawPathButton(g2d, bPathEnd, pathEnd, "End");
        
        // Draw action buttons
        drawActionButton(g2d, bValidate, "Validate");
        drawActionButton(g2d, bSave, "Save");
        drawActionButton(g2d, bCancel, "Cancel");
        
        // Draw selected tile preview
        drawSelectedTile(g2d);
        
        // Draw validation status
        drawValidationStatus(g2d);
    }

    private void drawPathButton(Graphics2D g2d, MyButton b, BufferedImage img, String label) {
        if (b == null) return;
        
        // Semi-transparent button background
        Color bgColor = b.isMouseOver() ? 
            new Color(255, 215, 0, 200) : 
            new Color(240, 240, 240, 120);
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
        
        // Make tile images smaller (increased padding)
        int padding = 12;
        int imageSize = b.width - (padding * 2);
        g2d.drawImage(img, b.x + padding, b.y + padding, imageSize, imageSize, null);
        
        // Border
        drawEnhancedButtonFeedback(g2d, b);
        
        // Label on hover
        if (b.isMouseOver()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, b.x + (b.width - labelWidth) / 2, b.y - 5);
        }
    }

    private void drawTileButton(Graphics2D g2d, MyButton b, String label) {
        if (b == null) return;
        
        // Semi-transparent button background
        Color bgColor = b.isMouseOver() ? 
            new Color(135, 206, 235, 200) : 
            new Color(240, 240, 240, 120);
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
        
        // Make tile images smaller
        BufferedImage sprite = getButtImg(b.getId());
        if (sprite != null) {
            int padding = 12;
            int imageSize = b.width - (padding * 2);
            g2d.drawImage(sprite, b.x + padding, b.y + padding, imageSize, imageSize, null);
        }
        
        // Border
        drawEnhancedButtonFeedback(g2d, b);
        
        // Label on hover
        if (b.isMouseOver()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, b.x + (b.width - labelWidth) / 2, b.y - 5);
        }
    }
    
    private void drawActionButton(Graphics2D g2d, MyButton b, String label) {
        if (b == null) return;
        
        // Semi-transparent button background
        Color bgColor;
        if (label.equals("Save")) {
            bgColor = b.isMouseOver() ? 
                new Color(76, 175, 80, 200) : 
                new Color(100, 180, 100, 150);
        } else if (label.equals("Validate")) {
            bgColor = b.isMouseOver() ? 
                new Color(135, 206, 235, 200) : 
                new Color(100, 160, 220, 150);
        } else {
            bgColor = b.isMouseOver() ? 
                new Color(255, 215, 0, 200) : 
                new Color(240, 240, 240, 150);
        }
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(b.x, b.y, b.width, b.height, 8, 8);
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int labelWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, b.x + (b.width - labelWidth) / 2, b.y + 22);
        
        // Border
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawRoundRect(b.x, b.y, b.width, b.height, 8, 8);
        
        if (b.isMousePressed()) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(b.x + 1, b.y + 1, b.width - 2, b.height - 2, 6, 6);
        }
    }

    private void drawMapButtons(Graphics2D g2d) {
        String[] labels = {"Road", "Corner", "Water C", "Beach", "Island"};
        int labelIndex = 0;
        
        MyButton[] buttons = {bRoadS, bRoadC, bWaterC, bWaterB, bWaterI};
        
        for (MyButton b : buttons) {
            if (b == null) continue;
            
            ArrayList<Tile> tileList = map.get(b);
            if (tileList == null || tileList.isEmpty()) continue;
            
            BufferedImage img = tileList.get(0).getSprite();

            // Semi-transparent button background
            boolean isSelected = (currentButton == b);
            Color bgColor;
            if (isSelected) {
                bgColor = new Color(76, 175, 80, 200);
            } else if (b.isMouseOver()) {
                bgColor = new Color(135, 206, 235, 200);
            } else {
                bgColor = new Color(240, 240, 240, 120);
            }
            
            g2d.setColor(bgColor);
            g2d.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
            
            // Make tile images smaller
            int padding = 12;
            int imageSize = b.width - (padding * 2);
            g2d.drawImage(img, b.x + padding, b.y + padding, imageSize, imageSize, null);
            
            // Border
            drawEnhancedButtonFeedback(g2d, b);
            
            // Rotation indicator for selected button
            if (isSelected) {
                g2d.setColor(new Color(255, 215, 0));
                int indicatorSize = 6;
                g2d.fillOval(b.x + b.width - indicatorSize - 3, b.y + 3, indicatorSize, indicatorSize);
                g2d.setColor(Color.WHITE);
                g2d.drawOval(b.x + b.width - indicatorSize - 3, b.y + 3, indicatorSize, indicatorSize);
            }
            
            // Label on hover
            if (b.isMouseOver() && labelIndex < labels.length) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                int labelWidth = g2d.getFontMetrics().stringWidth(labels[labelIndex]);
                g2d.drawString(labels[labelIndex], b.x + (b.width - labelWidth) / 2, b.y - 5);
            }
            labelIndex++;
        }
    }

    private void drawSelectedTile(Graphics2D g2d) {
        if (selectedTile != null) {
            // Panel for selected tile preview - positioned at right side of toolbar
            int panelX = 540;
            int panelY = y + 5;
            int panelW = 90;
            int panelH = 80;
            
            // Semi-transparent panel
            g2d.setColor(new Color(240, 240, 240, 120));
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 10, 10);
            
            // Label
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            String label = "Selected:";
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, panelX + (panelW - labelWidth) / 2, panelY + 15);
            
            // Tile preview - smaller size
            int tileSize = 40;
            int tileX = panelX + (panelW - tileSize) / 2;
            int tileY = panelY + 25;
            
            // Checkered background for better visibility (semi-transparent)
            g2d.setColor(new Color(200, 200, 200, 80));
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    if ((i + j) % 2 == 0) {
                        g2d.fillRect(tileX + i * tileSize / 2, tileY + j * tileSize / 2, 
                                   tileSize / 2, tileSize / 2);
                    }
                }
            }
            
            // Selected tile
            g2d.drawImage(selectedTile.getSprite(), tileX, tileY, tileSize, tileSize, null);
            
            // Border around tile
            g2d.setColor(new Color(76, 175, 80, 200));
            g2d.drawRoundRect(tileX - 1, tileY - 1, tileSize + 2, tileSize + 2, 6, 6);
        }
    }
    
    private void drawValidationStatus(Graphics2D g2d) {
        if (mapEditor.getValidationMessage() != null && !mapEditor.getValidationMessage().isEmpty()) {
            int statusX = 540;
            int statusY = y + 90;
            int statusW = 90;
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.setColor(mapEditor.isValidPath() ? 
                new Color(76, 175, 80) : 
                new Color(231, 76, 60));
            
            String status = mapEditor.isValidPath() ? "✓ Valid" : "✗ Invalid";
            int statusWidth = g2d.getFontMetrics().stringWidth(status);
            g2d.drawString(status, statusX + (statusW - statusWidth) / 2, statusY);
        }
    }

    private void drawEnhancedButtonFeedback(Graphics2D g2d, MyButton b) {
        if (b.isMousePressed()) {
            // Pressed state
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(b.x + 1, b.y + 1, b.width - 2, b.height - 2, 8, 8);
        } else if (b.isMouseOver()) {
            // Hover state
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);
        } else {
            // Normal state
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);
        }
    }

    private Color darken(Color c, float factor) {
        return new Color(
            Math.max((int)(c.getRed() * factor), 0),
            Math.max((int)(c.getGreen() * factor), 0),
            Math.max((int)(c.getBlue() * factor), 0),
            c.getAlpha()
        );
    }

    public BufferedImage getButtImg(int id) {
        return getTileManager().getSprite(id);
    }

    public void mouseClicked(int x, int y) {
        if (bValidate.getBounds().contains(x, y)) {
            mapEditor.validateLevel();
        } else if (bSave.getBounds().contains(x, y)) {
            mapEditor.saveLevel();
        } else if (bCancel.getBounds().contains(x, y)) {
            // Cancel handled by AdminPanel
        } else if (bWater.getBounds().contains(x, y)) {
            selectedTile = getTileManager().getTile(bWater.getId());
            mapEditor.setSelectedTile(selectedTile);
        } else if (bGrass.getBounds().contains(x, y)) {
            selectedTile = getTileManager().getTile(bGrass.getId());
            mapEditor.setSelectedTile(selectedTile);
        } else if (bPathStart.getBounds().contains(x, y)) {
            selectedTile = new Tile(pathStart, -1, -1);
            mapEditor.setSelectedTile(selectedTile);
        } else if (bPathEnd.getBounds().contains(x, y)) {
            selectedTile = new Tile(pathEnd, -2, -2);
            mapEditor.setSelectedTile(selectedTile);
        } else {
            for (MyButton b : map.keySet()) {
                if (b.getBounds().contains(x, y)) {
                    selectedTile = map.get(b).get(0);
                    mapEditor.setSelectedTile(selectedTile);
                    currentButton = b;
                    currentIndex = 0;
                    return;
                }
            }
        }
    }

    public void mouseMoved(int x, int y) {
        // Reset all button states
        bValidate.setMouseOver(false);
        bSave.setMouseOver(false);
        bCancel.setMouseOver(false);
        bWater.setMouseOver(false);
        bGrass.setMouseOver(false);
        bPathStart.setMouseOver(false);
        bPathEnd.setMouseOver(false);

        for (MyButton b : map.keySet()) {
            b.setMouseOver(false);
        }

        // Set mouse over for appropriate button
        if (bValidate.getBounds().contains(x, y)) {
            bValidate.setMouseOver(true);
        } else if (bSave.getBounds().contains(x, y)) {
            bSave.setMouseOver(true);
        } else if (bCancel.getBounds().contains(x, y)) {
            bCancel.setMouseOver(true);
        } else if (bWater.getBounds().contains(x, y)) {
            bWater.setMouseOver(true);
        } else if (bGrass.getBounds().contains(x, y)) {
            bGrass.setMouseOver(true);
        } else if (bPathStart.getBounds().contains(x, y)) {
            bPathStart.setMouseOver(true);
        } else if (bPathEnd.getBounds().contains(x, y)) {
            bPathEnd.setMouseOver(true);
        } else {
            for (MyButton b : map.keySet()) {
                if (b.getBounds().contains(x, y)) {
                    b.setMouseOver(true);
                    return;
                }
            }
        }
    }

    public void mousePressed(int x, int y) {
        if (bValidate.getBounds().contains(x, y)) {
            bValidate.setMousePressed(true);
        } else if (bSave.getBounds().contains(x, y)) {
            bSave.setMousePressed(true);
        } else if (bCancel.getBounds().contains(x, y)) {
            bCancel.setMousePressed(true);
        } else if (bWater.getBounds().contains(x, y)) {
            bWater.setMousePressed(true);
        } else if (bGrass.getBounds().contains(x, y)) {
            bGrass.setMousePressed(true);
        } else if (bPathStart.getBounds().contains(x, y)) {
            bPathStart.setMousePressed(true);
        } else if (bPathEnd.getBounds().contains(x, y)) {
            bPathEnd.setMousePressed(true);
        } else {
            for (MyButton b : map.keySet()) {
                if (b.getBounds().contains(x, y)) {
                    b.setMousePressed(true);
                    return;
                }
            }
        }
    }

    public void mouseReleased(int x, int y) {
        bValidate.resetBooleans();
        bSave.resetBooleans();
        bCancel.resetBooleans();
        bGrass.resetBooleans();
        bWater.resetBooleans();
        bPathStart.resetBooleans();
        bPathEnd.resetBooleans();
        
        for (MyButton b : map.keySet()) {
            b.resetBooleans();
        }
    }

    public BufferedImage getStartPathImg() {
        return pathStart;
    }

    public BufferedImage getEndPathImg() {
        return pathEnd;
    }

    // Getters for buttons (needed by MapEditor)
    public MyButton getValidateButton() { return bValidate; }
    public MyButton getSaveButton() { return bSave; }
    public MyButton getCancelButton() { return bCancel; }
}