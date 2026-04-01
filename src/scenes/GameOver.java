package scenes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;

import main.Game;
import managers.MusicManager;  // ← ADD THIS IMPORT
import ui.MyButton;
import static main.GameStates.*;

public class GameOver extends GameScene implements SceneMethods {

	private MyButton bReplay, bMenu;
	private Font titleFont, dialogueFont, buttonFont;
	private BufferedImage kingImg;
	private MusicManager musicManager;  // ← ADD THIS FIELD

	public GameOver(Game game) {
		super(game);
		musicManager = MusicManager.getInstance();  // ← ADD THIS LINE
		loadImages();
		loadFonts();
		initButtons();
	}

	private void loadImages() {
		try {
			File kingFile = new File("res/kingsad1.png");
			if (kingFile.exists()) {
				kingImg = ImageIO.read(kingFile);
				System.out.println("✅ Successfully loaded kingsad1.png");
			} else {
				System.out.println("⚠️ Warning: Could not find kingsad1.png at " + kingFile.getAbsolutePath());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("❌ Error loading gameover images");
		}
	}	

	private void loadFonts() {
		titleFont = new Font("Arial", Font.BOLD, 60);
		dialogueFont = new Font("Arial", Font.BOLD, 20);
		buttonFont = new Font("Arial", Font.BOLD, 20);
	}

	private void initButtons() {
		int w = 200;
		int h = 50;
		int x = 640 / 2 - w / 2;
		int y = 620;
		int yOffset = 70;

		bReplay = new MyButton("Replay", x, y, w, h);
		bMenu = new MyButton("Menu", x, y + yOffset, w, h);
	}

	// ← ADD THIS NEW METHOD
	/**
	 * Start game over music when entering this scene
	 * IMPORTANT: Convert gameover.mp3 to gameover.wav first!
	 */
	public void startGameOverMusic() {
		musicManager.playMusic("res/gameover.wav");
	}

	@Override
	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		drawBackground(g2d);
		drawTitle(g2d);
		drawKing(g2d);
		drawKingDialogue(g2d);
		drawButtons(g2d);
	}

	private void drawBackground(Graphics2D g2d) {
		java.awt.GradientPaint gradient = new java.awt.GradientPaint(
			0, 0, new Color(40, 20, 20),
			0, 800, new Color(60, 30, 30)
		);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, 640, 800);
		
		g2d.setColor(new Color(0, 0, 0, 50));
		for (int i = 0; i < 5; i++) {
			int x = i * 150 - 50;
			int y = 100 + i * 30;
			g2d.fillOval(x, y, 200, 100);
		}
	}

	private void drawTitle(Graphics2D g2d) {
		String title = "GAME OVER";
		
		g2d.setFont(titleFont);
		
		g2d.setColor(new Color(0, 0, 0, 200));
		int titleX = 640 / 2 - g2d.getFontMetrics().stringWidth(title) / 2;
		g2d.drawString(title, titleX + 3, 83);
		
		g2d.setColor(new Color(180, 50, 50));
		g2d.drawString(title, titleX, 80);
		
		g2d.setFont(new Font("Arial", Font.ITALIC, 24));
		String subtitle = "The kingdom has fallen...";
		int subtitleX = 640 / 2 - g2d.getFontMetrics().stringWidth(subtitle) / 2;
		g2d.setColor(new Color(150, 150, 150));
		g2d.drawString(subtitle, subtitleX, 125);
	}

	private void drawKing(Graphics2D g2d) {
		if (kingImg != null) {
			int kingWidth = 180;
			int kingHeight = (int) (kingImg.getHeight() * ((float) kingWidth / kingImg.getWidth()));
			int kingX = 640 / 2 - kingWidth / 2;
			int kingY = 170;
			
			g2d.drawImage(kingImg, kingX, kingY, kingWidth, kingHeight, null);
		}
	}

	private void drawKingDialogue(Graphics2D g2d) {
		int boxX = 140;
		int boxY = 390;
		int boxWidth = 360;
		int boxHeight = 100;
		
		g2d.setColor(new Color(0, 0, 0, 150));
		g2d.fillRoundRect(boxX + 5, boxY + 5, boxWidth, boxHeight, 20, 20);
		
		g2d.setColor(new Color(200, 200, 200, 250));
		g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
		
		g2d.setColor(new Color(80, 60, 60));
		g2d.setStroke(new java.awt.BasicStroke(4));
		g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
		
		int[] triX = {320 - 15, 320, 320 + 15};
		int[] triY = {boxY, boxY - 20, boxY};
		g2d.setColor(new Color(200, 200, 200, 250));
		g2d.fillPolygon(triX, triY, 3);
		g2d.setColor(new Color(80, 60, 60));
		g2d.drawPolygon(triX, triY, 3);
		
		g2d.setFont(dialogueFont);
		g2d.setColor(new Color(40, 40, 40));
		String dialogue = "Our kingdom is doomed...";
		int textX = boxX + (boxWidth - g2d.getFontMetrics().stringWidth(dialogue)) / 2;
		g2d.drawString(dialogue, textX, boxY + 45);
		
		g2d.setFont(new Font("Arial", Font.ITALIC, 18));
		String dialogue2 = "Will you try again?";
		textX = boxX + (boxWidth - g2d.getFontMetrics().stringWidth(dialogue2)) / 2;
		g2d.drawString(dialogue2, textX, boxY + 75);
		
		g2d.setColor(new Color(80, 60, 60));
		g2d.fillRoundRect(boxX + 10, boxY - 15, 100, 30, 10, 10);
		g2d.setFont(new Font("Arial", Font.BOLD, 16));
		g2d.setColor(new Color(200, 200, 200));
		g2d.drawString("King", boxX + 35, boxY + 7);
	}

	private void drawButtons(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setFont(buttonFont);
		
		drawEnhancedButton(g2d, bReplay);
		drawEnhancedButton(g2d, bMenu);
	}

	private void drawEnhancedButton(Graphics2D g2d, MyButton button) {
		int x = button.getBounds().x;
		int y = button.getBounds().y;
		int w = button.getBounds().width;
		int h = button.getBounds().height;
		
		Color baseColor, borderColor, textColor;
		int elevation = 0;
		
		if (button.isMousePressed()) {
			baseColor = new Color(150, 50, 50);
			borderColor = new Color(120, 40, 40);
			textColor = Color.WHITE;
			elevation = -2;
		} else if (button.isMouseOver()) {
			baseColor = new Color(180, 70, 70);
			borderColor = new Color(150, 50, 50);
			textColor = Color.WHITE;
			elevation = 2;
		} else {
			baseColor = new Color(160, 60, 60);
			borderColor = new Color(130, 50, 50);
			textColor = new Color(230, 230, 230);
		}
		
		g2d.setColor(new Color(0, 0, 0, 100));
		g2d.fillRoundRect(x + 4, y + 4 - elevation, w, h, 15, 15);
		
		g2d.setColor(baseColor);
		g2d.fillRoundRect(x, y - elevation, w, h, 15, 15);
		
		g2d.setColor(borderColor);
		g2d.setStroke(new java.awt.BasicStroke(3));
		g2d.drawRoundRect(x, y - elevation, w, h, 15, 15);
		
		g2d.setColor(textColor);
		g2d.setFont(buttonFont);
		String text = button.getText();
		int textX = x + (w - g2d.getFontMetrics().stringWidth(text)) / 2;
		int textY = y - elevation + (h + g2d.getFontMetrics().getAscent()) / 2 - 2;
		
		g2d.setColor(new Color(0, 0, 0, 100));
		g2d.drawString(text, textX + 1, textY + 1);
		
		g2d.setColor(textColor);
		g2d.drawString(text, textX, textY);
	}

	private void replayGame() {
		musicManager.stopMusic();  // ← STOP GAME OVER MUSIC
		resetAll();
		setGameState(PLAYING);
		// Playing scene will start its own music
	}

	private void resetAll() {
		game.getPlaying().resetEverything();
	}

	@Override
	public void mouseClicked(int x, int y) {
		if (bMenu.getBounds().contains(x, y)) {
			musicManager.stopMusic();  // ← STOP MUSIC WHEN GOING TO MENU
			setGameState(MENU);
			resetAll();
		} else if (bReplay.getBounds().contains(x, y))
			replayGame();
	}

	@Override
	public void mouseMoved(int x, int y) {
		bMenu.setMouseOver(false);
		bReplay.setMouseOver(false);

		if (bMenu.getBounds().contains(x, y))
			bMenu.setMouseOver(true);
		else if (bReplay.getBounds().contains(x, y))
			bReplay.setMouseOver(true);
	}

	@Override
	public void mousePressed(int x, int y) {
		if (bMenu.getBounds().contains(x, y))
			bMenu.setMousePressed(true);
		else if (bReplay.getBounds().contains(x, y))
			bReplay.setMousePressed(true);
	}

	@Override
	public void mouseReleased(int x, int y) {
		bMenu.resetBooleans();
		bReplay.resetBooleans();
	}

	@Override
	public void mouseDragged(int x, int y) {
		// Not needed
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			musicManager.stopMusic();  // ← STOP MUSIC ON ESC
			setGameState(MENU);
		}
	}
}