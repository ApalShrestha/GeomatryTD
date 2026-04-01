package scenes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import main.Game;
import managers.MusicManager;
import ui.MyButton;

import static main.GameStates.*;

public class Settings extends GameScene implements SceneMethods {

	private MyButton bMenu, bMusicToggle, bVolumeUp, bVolumeDown;
	private MusicManager musicManager;

	public Settings(Game game) {
		super(game);
		musicManager = MusicManager.getInstance();
		initButtons();
	}

	private void initButtons() {
		int centerX = 320;
		int startY = 250;
		int spacing = 80;
		
		bMenu = new MyButton("Back to Menu", centerX - 100, startY + spacing * 3, 200, 50);
		bMusicToggle = new MyButton(musicManager.isMusicEnabled() ? "Music: ON" : "Music: OFF", 
									 centerX - 100, startY, 200, 50);
		bVolumeDown = new MyButton("-", centerX - 150, startY + spacing, 40, 50);
		bVolumeUp = new MyButton("+", centerX + 110, startY + spacing, 40, 50);
	}

	@Override
	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Vibrant gradient background
		GradientPaint bgGradient = new GradientPaint(
			0, 0, new Color(45, 52, 93),      // Dark blue-purple
			0, 800, new Color(120, 81, 169)   // Purple
		);
		g2d.setPaint(bgGradient);
		g2d.fillRect(0, 0, 640, 800);
		
		// Animated circles background effect
		g2d.setColor(new Color(255, 255, 255, 20));
		for (int i = 0; i < 5; i++) {
			int size = 100 + i * 80;
			g2d.fillOval(320 - size/2 + (int)(Math.sin(System.currentTimeMillis() / 1000.0 + i) * 30), 
						 400 - size/2, size, size);
		}
		
		// Title with shadow
		g2d.setFont(new Font("Arial", Font.BOLD, 48));
		String title = "SETTINGS";
		int titleWidth = g2d.getFontMetrics().stringWidth(title);
		
		// Shadow
		g2d.setColor(new Color(0, 0, 0, 100));
		g2d.drawString(title, 320 - titleWidth/2 + 3, 103);
		
		// Title gradient
		GradientPaint titleGradient = new GradientPaint(
			0, 70, new Color(255, 215, 0),
			0, 110, new Color(255, 165, 0)
		);
		g2d.setPaint(titleGradient);
		g2d.drawString(title, 320 - titleWidth/2, 100);
		
		// Music section
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 20));
		g2d.drawString("Background Music", 220, 230);
		
		// Volume bar background
		g2d.setColor(new Color(255, 255, 255, 50));
		g2d.fillRoundRect(220, 310, 200, 30, 15, 15);
		
		// Volume bar fill
		int volumeWidth = (int)(200 * musicManager.getVolume());
		GradientPaint volumeGradient = new GradientPaint(
			220, 310, new Color(76, 175, 80),
			220 + volumeWidth, 310, new Color(139, 195, 74)
		);
		g2d.setPaint(volumeGradient);
		g2d.fillRoundRect(220, 310, volumeWidth, 30, 15, 15);
		
		// Volume percentage
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 16));
		String volumeText = musicManager.getVolumePercent() + "%";
		int volTextWidth = g2d.getFontMetrics().stringWidth(volumeText);
		g2d.drawString(volumeText, 320 - volTextWidth/2, 332);
		
		// Instructions
		g2d.setFont(new Font("Arial", Font.ITALIC, 14));
		g2d.setColor(new Color(200, 200, 200));
		g2d.drawString("Use +/- buttons or Arrow keys to adjust volume", 180, 380);
		g2d.drawString("Press M to toggle music on/off", 220, 400);
		
		// Draw buttons
		drawButtons(g);
	}

	private void drawButtons(Graphics g) {
		bMusicToggle.draw(g);
		bVolumeDown.draw(g);
		bVolumeUp.draw(g);
		bMenu.draw(g);
	}

	@Override
	public void mouseClicked(int x, int y) {
		if (bMenu.getBounds().contains(x, y)) {
			setGameState(MENU);
		} else if (bMusicToggle.getBounds().contains(x, y)) {
			musicManager.toggleMusic();
			updateButtonTexts();
		} else if (bVolumeUp.getBounds().contains(x, y)) {
			musicManager.increaseVolume();
		} else if (bVolumeDown.getBounds().contains(x, y)) {
			musicManager.decreaseVolume();
		}
		getGame().repaint();
	}

	@Override
	public void mouseMoved(int x, int y) {
		bMenu.setMouseOver(bMenu.getBounds().contains(x, y));
		bMusicToggle.setMouseOver(bMusicToggle.getBounds().contains(x, y));
		bVolumeUp.setMouseOver(bVolumeUp.getBounds().contains(x, y));
		bVolumeDown.setMouseOver(bVolumeDown.getBounds().contains(x, y));
		getGame().repaint();
	}

	@Override
	public void mousePressed(int x, int y) {
		if (bMenu.getBounds().contains(x, y))
			bMenu.setMousePressed(true);
		else if (bMusicToggle.getBounds().contains(x, y))
			bMusicToggle.setMousePressed(true);
		else if (bVolumeUp.getBounds().contains(x, y))
			bVolumeUp.setMousePressed(true);
		else if (bVolumeDown.getBounds().contains(x, y))
			bVolumeDown.setMousePressed(true);
		
		getGame().repaint();
	}

	@Override
	public void mouseReleased(int x, int y) {
		resetButtons();
		getGame().repaint();
	}

	private void resetButtons() {
		bMenu.resetBooleans();
		bMusicToggle.resetBooleans();
		bVolumeUp.resetBooleans();
		bVolumeDown.resetBooleans();
	}

	@Override
	public void mouseDragged(int x, int y) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setGameState(MENU);
		} else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_RIGHT) {
			musicManager.increaseVolume();
			getGame().repaint();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT) {
			musicManager.decreaseVolume();
			getGame().repaint();
		} else if (e.getKeyCode() == KeyEvent.VK_M) {
			musicManager.toggleMusic();
			updateButtonTexts();
			getGame().repaint();
		}
	}
	
	private void updateButtonTexts() {
		bMusicToggle.setText(musicManager.isMusicEnabled() ? "Music: ON" : "Music: OFF");
	}
}