package ui;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.FontMetrics;


public class MyButton {


    public int x, y, width, height, id;
    private String text;
    private Rectangle bounds;
    private boolean mouseOver, mousePressed;
   
    // Animation variables
    private float hoverScale = 1.0f;
    private float targetScale = 1.0f;
    private int shadowOffset = 4;


    // Color scheme - Minecraft/Geomatry inspired
    private static final Color BUTTON_BASE = new Color(255, 255, 255, 240);
    private static final Color BUTTON_HOVER = new Color(124, 200, 80); // Grass green
    private static final Color BUTTON_PRESSED = new Color(100, 180, 60);
    private static final Color BORDER_COLOR = new Color(20, 70, 120); // Dark blue
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);
    private static final Color TEXT_COLOR = new Color(30, 60, 100);
    private static final Color TEXT_HOVER = new Color(255, 255, 255);


    // For normal Buttons
    public MyButton(String text, int x, int y, int width, int height) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = -1;


        initBounds();
    }


    // For tile buttons
    public MyButton(String text, int x, int y, int width, int height, int id) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = id;


        initBounds();
    }


    private void initBounds() {
        this.bounds = new Rectangle(x, y, width, height);
    }
   
    // Smooth animation update
    public void update() {
        targetScale = mouseOver ? 1.05f : 1.0f;
        hoverScale += (targetScale - hoverScale) * 0.3f;
    }


    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
       
        update();
       
        int scaledWidth = (int)(width * hoverScale);
        int scaledHeight = (int)(height * hoverScale);
        int offsetX = (width - scaledWidth) / 2;
        int offsetY = (height - scaledHeight) / 2;
        int drawX = x + offsetX;
        int drawY = y + offsetY;


        // Draw shadow
        if (!mousePressed) {
            g2d.setColor(SHADOW_COLOR);
            g2d.fillRoundRect(drawX + shadowOffset, drawY + shadowOffset,
                            scaledWidth, scaledHeight, 12, 12);
        }


        // Draw body with gradient
        drawBody(g2d, drawX, drawY, scaledWidth, scaledHeight);


        // Draw border with multiple layers for depth
        drawBorder(g2d, drawX, drawY, scaledWidth, scaledHeight);


        // Draw text
        drawText(g2d, drawX, drawY, scaledWidth, scaledHeight);
    }


    private void drawBorder(Graphics2D g2d, int drawX, int drawY, int scaledWidth, int scaledHeight) {
        // Outer border - dark blue
        g2d.setColor(BORDER_COLOR);
        g2d.drawRoundRect(drawX, drawY, scaledWidth, scaledHeight, 12, 12);
       
        if (mousePressed) {
            // Inner pressed effect
            g2d.setColor(new Color(0, 0, 0, 40));
            g2d.drawRoundRect(drawX + 2, drawY + 2, scaledWidth - 4, scaledHeight - 4, 10, 10);
            g2d.drawRoundRect(drawX + 3, drawY + 3, scaledWidth - 6, scaledHeight - 6, 9, 9);
        } else if (mouseOver) {
            // Bright highlight on hover
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(drawX + 1, drawY + 1, scaledWidth - 2, scaledHeight - 2, 11, 11);
        }
    }


    private void drawBody(Graphics2D g2d, int drawX, int drawY, int scaledWidth, int scaledHeight) {
        Color bodyColor;
       
        if (mousePressed) {
            bodyColor = BUTTON_PRESSED;
        } else if (mouseOver) {
            bodyColor = BUTTON_HOVER;
        } else {
            bodyColor = BUTTON_BASE;
        }
       
        // Gradient for depth
        GradientPaint gradient = new GradientPaint(
            drawX, drawY, bodyColor,
            drawX, drawY + scaledHeight, darken(bodyColor, 0.9f)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(drawX, drawY, scaledWidth, scaledHeight, 12, 12);
       
        // Top highlight for 3D effect
        if (!mousePressed) {
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fillRoundRect(drawX + 2, drawY + 2, scaledWidth - 4, scaledHeight / 3, 10, 10);
        }
    }


    private void drawText(Graphics2D g2d, int drawX, int drawY, int scaledWidth, int scaledHeight) {
        if (text == null || text.isEmpty()) return;
       
        // Use bold font for better readability
        Font font = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(font);
       
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
       
        int textX = drawX + (scaledWidth - textWidth) / 2;
        int textY = drawY + (scaledHeight + textHeight / 2) / 2 - 2;
       
        // Text shadow for depth
        if (!mousePressed) {
            g2d.setColor(new Color(0, 0, 0, 60));
            g2d.drawString(text, textX + 1, textY + 1);
        }
       
        // Main text
        g2d.setColor(mouseOver ? TEXT_HOVER : TEXT_COLOR);
        g2d.drawString(text, textX, textY);
    }
   
    // Utility method to darken colors
    private Color darken(Color c, float factor) {
        return new Color(
            Math.max((int)(c.getRed() * factor), 0),
            Math.max((int)(c.getGreen() * factor), 0),
            Math.max((int)(c.getBlue() * factor), 0),
            c.getAlpha()
        );
    }


    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    public void resetBooleans() {
        this.mouseOver = false;
        this.mousePressed = false;
    }


    public void setText(String text) {
        this.text = text;
    }


    public String getText() {
        return text;
    }


    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }


    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }


    public boolean isMouseOver() {
        return mouseOver;
    }


    public boolean isMousePressed() {
        return mousePressed;
    }

    // FIX: sync bounds with current x/y/width/height before returning,
    // so hit-testing always reflects the button's actual position.
    public Rectangle getBounds() {
        bounds.x = x;
        bounds.y = y;
        bounds.width = width;
        bounds.height = height;
        return bounds;
    }


    public int getId() {
        return id;
    }
}