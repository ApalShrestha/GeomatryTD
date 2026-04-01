package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;

public class Bar {

	protected int x, y, width, height;
	
	// Common color constants
	protected static final Color SKY_BLUE = new Color(135, 206, 235);
	protected static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
	protected static final Color LIGHT_CREAM = new Color(255, 253, 208);
	protected static final Color DARK_BLUE = new Color(25, 55, 109);
	protected static final Color GRASS_GREEN = new Color(76, 175, 80);

	public Bar(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	protected void drawButtonFeedback(Graphics g, MyButton b) {
		// MouseOver
		if (b.isMouseOver())
			g.setColor(Color.white);
		else
			g.setColor(Color.BLACK);

		// Border
		g.drawRect(b.x, b.y, b.width, b.height);

		// MousePressed
		if (b.isMousePressed()) {
			g.drawRect(b.x + 1, b.y + 1, b.width - 2, b.height - 2);
			g.drawRect(b.x + 2, b.y + 2, b.width - 4, b.height - 4);
		}
	}

	// New enhanced helper methods
	protected Color darken(Color color, float factor) {
		int r = Math.max((int)(color.getRed() * factor), 0);
		int g = Math.max((int)(color.getGreen() * factor), 0);
		int b = Math.max((int)(color.getBlue() * factor), 0);
		return new Color(r, g, b);
	}

	protected void drawGradientBackground(Graphics2D g2d, int x, int y, int width, int height, 
										Color startColor, Color endColor) {
		GradientPaint gradient = new GradientPaint(
			x, y, startColor,
			x, y + height, endColor
		);
		g2d.setPaint(gradient);
		g2d.fillRect(x, y, width, height);
	}

	protected void drawPanel(Graphics2D g2d, int x, int y, int width, int height, Color color) {
		// Shadow
		g2d.setColor(new Color(0, 0, 0, 60));
		g2d.fillRoundRect(x + 3, y + 3, width, height, 15, 15);
		
		// Panel background
		GradientPaint gradient = new GradientPaint(
			x, y, color,
			x, y + height, darken(color, 0.85f)
		);
		g2d.setPaint(gradient);
		g2d.fillRoundRect(x, y, width, height, 15, 15);
		
		// Border
		g2d.setColor(darken(color, 0.7f));
		g2d.drawRoundRect(x, y, width, height, 15, 15);
	}

	protected void drawEnhancedButtonFeedback(Graphics2D g2d, MyButton b) {
		// Enable anti-aliasing for smoother graphics
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Border
		if (b.isMouseOver()) {
			g2d.setColor(GOLDEN_YELLOW);
		} else {
			g2d.setColor(darken(LIGHT_CREAM, 0.8f));
		}
		g2d.drawRoundRect(b.x, b.y, b.width, b.height, 12, 12);
		
		// Pressed effect
		if (b.isMousePressed()) {
			g2d.setColor(new Color(0, 0, 0, 50));
			g2d.fillRoundRect(b.x, b.y, b.width, b.height, 12, 12);
		}
	}

	// Getters
	public int getX() { return x; }
	public int getY() { return y; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
}