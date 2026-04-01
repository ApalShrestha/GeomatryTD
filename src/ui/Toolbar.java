package ui;

import static main.GameStates.MENU;
import static main.GameStates.setGameState;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import helpz.LoadSave;
import objects.Tile;
import scenes.Editing;

public class Toolbar extends Bar {
	private Editing editing;
	private MyButton bMenu, bSave;
	private MyButton bPathStart, bPathEnd;
	private BufferedImage pathStart, pathEnd;
	private Tile selectedTile;

	private Map<MyButton, ArrayList<Tile>> map = new HashMap<MyButton, ArrayList<Tile>>();

	private MyButton bGrass, bWater, bRoadS, bRoadC, bWaterC, bWaterB, bWaterI;
	private MyButton currentButton;
	private int currentIndex = 0;
	
	private int animTick = 0;

	public Toolbar(int x, int y, int width, int height, Editing editing) {
		super(x, y, width, height);
		this.editing = editing;
		initPathImgs();
		initButtons();
	}

	private void initPathImgs() {
		pathStart = LoadSave.getSpriteAtlas().getSubimage(7 * 32, 2 * 32, 32, 32);
		pathEnd = LoadSave.getSpriteAtlas().getSubimage(8 * 32, 2 * 32, 32, 32);
	}

	private void initButtons() {
		bMenu = new MyButton("Menu", 2, 642, 100, 30);
		bSave = new MyButton("Save", 2, 674, 100, 30);

		int w = 50;
		int h = 50;
		int xStart = 110;
		int yStart = 650;
		int xOffset = (int) (w * 1.1f);
		int i = 0;

		bGrass = new MyButton("Grass", xStart, yStart, w, h, i++);
		bWater = new MyButton("Water", xStart + xOffset, yStart, w, h, i++);

		initMapButton(bRoadS, editing.getGame().getTileManager().getRoadsS(), xStart, yStart, xOffset, w, h, i++);
		initMapButton(bRoadC, editing.getGame().getTileManager().getRoadsC(), xStart, yStart, xOffset, w, h, i++);
		initMapButton(bWaterC, editing.getGame().getTileManager().getCorners(), xStart, yStart, xOffset, w, h, i++);
		initMapButton(bWaterB, editing.getGame().getTileManager().getBeaches(), xStart, yStart, xOffset, w, h, i++);
		initMapButton(bWaterI, editing.getGame().getTileManager().getIslands(), xStart, yStart, xOffset, w, h, i++);

		bPathStart = new MyButton("PathStart", xStart, yStart + xOffset, w, h, i++);
		bPathEnd = new MyButton("PathEnd", xStart + xOffset, yStart + xOffset, w, h, i++);
	}

	private void initMapButton(MyButton b, ArrayList<Tile> list, int x, int y, int xOff, int w, int h, int id) {
		b = new MyButton("", x + xOff * id, y, w, h, id);
		map.put(b, list);
	}

	private void saveLevel() {
		editing.saveLevel();
	}

	public void rotateSprite() {
		currentIndex++;
		if (currentIndex >= map.get(currentButton).size())
			currentIndex = 0;
		selectedTile = map.get(currentButton).get(currentIndex);
		editing.setSelectedTile(selectedTile);
	}

	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		animTick++;

		// Background with gradient - peaceful grass/sky theme
		drawGradientBackground(g2d, x, y, width, height, 
			new Color(100, 180, 120), new Color(80, 150, 90));
		
		// Top decorative stripe
		g2d.setColor(SKY_BLUE);
		g2d.fillRect(x, y, width, 4);

		// Buttons
		drawButtons(g2d);
	}

	private void drawButtons(Graphics2D g2d) {
		bMenu.draw(g2d);
		bSave.draw(g2d);

		drawPathButton(g2d, bPathStart, pathStart, "Start");
		drawPathButton(g2d, bPathEnd, pathEnd, "End");

		drawTileButton(g2d, bGrass, "Grass");
		drawTileButton(g2d, bWater, "Water");
		
		drawSelectedTile(g2d);
		drawMapButtons(g2d);
	}

	private void drawPathButton(Graphics2D g2d, MyButton b, BufferedImage img, String label) {
		// Shadow
		g2d.setColor(new Color(0, 0, 0, 60));
		g2d.fillRoundRect(b.x + 3, b.y + 3, b.width, b.height, 12, 12);
		
		// Background
		Color bgColor = b.isMouseOver() ? GOLDEN_YELLOW : LIGHT_CREAM;
		GradientPaint gradient = new GradientPaint(
			b.x, b.y, bgColor,
			b.x, b.y + b.height, darken(bgColor, 0.85f)
		);
		g2d.setPaint(gradient);
		g2d.fillRoundRect(b.x, b.y, b.width, b.height, 12, 12);
		
		// Image
		g2d.drawImage(img, b.x + 5, b.y + 5, b.width - 10, b.height - 10, null);
		
		// Border
		drawEnhancedButtonFeedback(g2d, b);
		
		// Label on hover
		if (b.isMouseOver()) {
			g2d.setColor(DARK_BLUE);
			g2d.setFont(new Font("Arial", Font.BOLD, 10));
			int labelWidth = g2d.getFontMetrics().stringWidth(label);
			g2d.drawString(label, b.x + (b.width - labelWidth) / 2, b.y - 5);
		}
	}

	private void drawTileButton(Graphics2D g2d, MyButton b, String label) {
		// Shadow
		g2d.setColor(new Color(0, 0, 0, 60));
		g2d.fillRoundRect(b.x + 3, b.y + 3, b.width, b.height, 12, 12);
		
		// Background with gradient
		Color bgColor = b.isMouseOver() ? SKY_BLUE : new Color(240, 240, 240);
		GradientPaint gradient = new GradientPaint(
			b.x, b.y, bgColor,
			b.x, b.y + b.height, darken(bgColor, 0.85f)
		);
		g2d.setPaint(gradient);
		g2d.fillRoundRect(b.x, b.y, b.width, b.height, 12, 12);
		
		// Tile sprite
		g2d.drawImage(getButtImg(b.getId()), b.x + 5, b.y + 5, b.width - 10, b.height - 10, null);
		
		// Border
		drawEnhancedButtonFeedback(g2d, b);
		
		// Label on hover
		if (b.isMouseOver()) {
			g2d.setColor(DARK_BLUE);
			g2d.setFont(new Font("Arial", Font.BOLD, 10));
			int labelWidth = g2d.getFontMetrics().stringWidth(label);
			g2d.drawString(label, b.x + (b.width - labelWidth) / 2, b.y - 5);
		}
	}

	private void drawMapButtons(Graphics2D g2d) {
		String[] labels = {"Road", "Corner", "Water C", "Beach", "Island"};
		int labelIndex = 0;
		
		for (Map.Entry<MyButton, ArrayList<Tile>> entry : map.entrySet()) {
			MyButton b = entry.getKey();
			BufferedImage img = entry.getValue().get(0).getSprite();

			// Shadow
			g2d.setColor(new Color(0, 0, 0, 60));
			g2d.fillRoundRect(b.x + 3, b.y + 3, b.width, b.height, 12, 12);
			
			// Background
			boolean isSelected = (currentButton == b);
			Color bgColor;
			if (isSelected) {
				bgColor = GRASS_GREEN;
			} else if (b.isMouseOver()) {
				bgColor = SKY_BLUE;
			} else {
				bgColor = new Color(240, 240, 240);
			}
			
			GradientPaint gradient = new GradientPaint(
				b.x, b.y, bgColor,
				b.x, b.y + b.height, darken(bgColor, 0.85f)
			);
			g2d.setPaint(gradient);
			g2d.fillRoundRect(b.x, b.y, b.width, b.height, 12, 12);
			
			// Tile image
			g2d.drawImage(img, b.x + 5, b.y + 5, b.width - 10, b.height - 10, null);
			
			// Border
			drawEnhancedButtonFeedback(g2d, b);
			
			// Rotation indicator for selected button
			if (isSelected) {
				g2d.setColor(GOLDEN_YELLOW);
				int indicatorSize = 8;
				g2d.fillOval(b.x + b.width - indicatorSize - 3, b.y + 3, indicatorSize, indicatorSize);
				g2d.setColor(DARK_BLUE);
				g2d.drawOval(b.x + b.width - indicatorSize - 3, b.y + 3, indicatorSize, indicatorSize);
			}
			
			// Label on hover
			if (b.isMouseOver() && labelIndex < labels.length) {
				g2d.setColor(DARK_BLUE);
				g2d.setFont(new Font("Arial", Font.BOLD, 10));
				int labelWidth = g2d.getFontMetrics().stringWidth(labels[labelIndex]);
				g2d.drawString(labels[labelIndex], b.x + (b.width - labelWidth) / 2, b.y - 5);
			}
			labelIndex++;
		}
	}

	private void drawSelectedTile(Graphics2D g2d) {
		if (selectedTile != null) {
			// Panel for selected tile preview
			int panelX = 540;
			int panelY = 645;
			int panelW = 90;
			int panelH = 90;
			
			drawPanel(g2d, panelX, panelY, panelW, panelH, LIGHT_CREAM);
			
			// Label
			g2d.setColor(DARK_BLUE);
			g2d.setFont(new Font("Arial", Font.BOLD, 12));
			String label = "Selected:";
			int labelWidth = g2d.getFontMetrics().stringWidth(label);
			g2d.drawString(label, panelX + (panelW - labelWidth) / 2, panelY + 15);
			
			// Tile preview with slight pulsing effect
			float pulseScale = 1.0f + (float)Math.sin(animTick * 0.1) * 0.05f;
			int tileSize = (int)(50 * pulseScale);
			int tileX = panelX + (panelW - tileSize) / 2;
			int tileY = panelY + 30;
			
			// Checkered background for better visibility
			g2d.setColor(new Color(200, 200, 200));
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
			g2d.setColor(GRASS_GREEN);
			g2d.drawRoundRect(tileX - 2, tileY - 2, tileSize + 4, tileSize + 4, 8, 8);
		}
	}

	public BufferedImage getButtImg(int id) {
		return editing.getGame().getTileManager().getSprite(id);
	}

	public void mouseClicked(int x, int y) {
		if (bMenu.getBounds().contains(x, y))
			setGameState(MENU);
		else if (bSave.getBounds().contains(x, y))
			saveLevel();
		else if (bWater.getBounds().contains(x, y)) {
			selectedTile = editing.getGame().getTileManager().getTile(bWater.getId());
			editing.setSelectedTile(selectedTile);
			return;
		} else if (bGrass.getBounds().contains(x, y)) {
			selectedTile = editing.getGame().getTileManager().getTile(bGrass.getId());
			editing.setSelectedTile(selectedTile);
			return;
		} else if (bPathStart.getBounds().contains(x, y)) {
			selectedTile = new Tile(pathStart, -1, -1);
			editing.setSelectedTile(selectedTile);
		} else if (bPathEnd.getBounds().contains(x, y)) {
			selectedTile = new Tile(pathEnd, -2, -2);
			editing.setSelectedTile(selectedTile);
		} else {
			for (MyButton b : map.keySet())
				if (b.getBounds().contains(x, y)) {
					selectedTile = map.get(b).get(0);
					editing.setSelectedTile(selectedTile);
					currentButton = b;
					currentIndex = 0;
					return;
				}
		}
	}

	public void mouseMoved(int x, int y) {
		bMenu.setMouseOver(false);
		bSave.setMouseOver(false);
		bWater.setMouseOver(false);
		bGrass.setMouseOver(false);
		bPathStart.setMouseOver(false);
		bPathEnd.setMouseOver(false);

		for (MyButton b : map.keySet())
			b.setMouseOver(false);

		if (bMenu.getBounds().contains(x, y))
			bMenu.setMouseOver(true);
		else if (bSave.getBounds().contains(x, y))
			bSave.setMouseOver(true);
		else if (bWater.getBounds().contains(x, y))
			bWater.setMouseOver(true);
		else if (bGrass.getBounds().contains(x, y))
			bGrass.setMouseOver(true);
		else if (bPathStart.getBounds().contains(x, y))
			bPathStart.setMouseOver(true);
		else if (bPathEnd.getBounds().contains(x, y))
			bPathEnd.setMouseOver(true);
		else {
			for (MyButton b : map.keySet())
				if (b.getBounds().contains(x, y)) {
					b.setMouseOver(true);
					return;
				}
		}
	}

	public void mousePressed(int x, int y) {
		if (bMenu.getBounds().contains(x, y))
			bMenu.setMousePressed(true);
		else if (bSave.getBounds().contains(x, y))
			bSave.setMousePressed(true);
		else if (bWater.getBounds().contains(x, y))
			bWater.setMousePressed(true);
		else if (bGrass.getBounds().contains(x, y))
			bGrass.setMousePressed(true);
		else if (bPathStart.getBounds().contains(x, y))
			bPathStart.setMousePressed(true);
		else if (bPathEnd.getBounds().contains(x, y))
			bPathEnd.setMousePressed(true);
		else {
			for (MyButton b : map.keySet())
				if (b.getBounds().contains(x, y)) {
					b.setMousePressed(true);
					return;
				}
		}
	}

	public void mouseReleased(int x, int y) {
		bMenu.resetBooleans();
		bSave.resetBooleans();
		bGrass.resetBooleans();
		bWater.resetBooleans();
		bPathStart.resetBooleans();
		bPathEnd.resetBooleans();
		for (MyButton b : map.keySet())
			b.resetBooleans();
	}

	public BufferedImage getStartPathImg() {
		return pathStart;
	}

	public BufferedImage getEndPathImg() {
		return pathEnd;
	}
}