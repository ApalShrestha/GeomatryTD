package ui;

import static main.GameStates.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.text.DecimalFormat;

import helpz.Constants.Towers;
import managers.SessionManager;
import objects.Tower;
import scenes.Playing;

public class ActionBar extends Bar {

	private Playing playing;
	private MyButton bMenu, bPause, bLogout, bCloseTowerPopup;

	private MyButton[] towerButtons;
	private Tower selectedTower;
	private Tower displayedTower;
	private MyButton sellTower, upgradeTower;

	private DecimalFormat formatter;

	private int gold = 100;
	private boolean showTowerCost;
	private int towerCostType;

	private float goldAnimScale = 1.0f;
	private int lastGold = 100;
	private int animTick = 0;

	private int lives = 25;

	// Tower popup panel constants — single source of truth for both drawing and hit-testing
	private static final int TOWER_PANEL_X = 420;
	private static final int TOWER_PANEL_Y = 700;

	private static final Color SKY_BLUE = new Color(135, 206, 235);
	private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);
	private static final Color LIGHT_CREAM = new Color(255, 253, 208);
	private static final Color DARK_BLUE = new Color(25, 55, 109);
	private static final Color GRASS_GREEN = new Color(76, 175, 80);

	public ActionBar(int x, int y, int width, int height, Playing playing) {
		super(x, y, width, height);
		this.playing = playing;
		formatter = new DecimalFormat("0.0");

		initButtons();
	}

	public void resetEverything() {
		lives = 25;
		towerCostType = 0;
		showTowerCost = false;
		gold = 100;
		lastGold = 100;
		selectedTower = null;
		displayedTower = null;
	}

	private void initButtons() {
		bMenu = new MyButton("Menu", 2, 642, 100, 30);
		bPause = new MyButton("Pause", 2, 682, 100, 30);
		bLogout = new MyButton("Logout", 2, 722, 100, 30);

		// Close button and sell/upgrade are positioned by updateTowerButtonBounds(),
		// but we still need to create them with placeholder geometry.
		bCloseTowerPopup = new MyButton("✕", 0, 0, 20, 20);
		sellTower        = new MyButton("Sell",    0, 0, 95, 20);
		upgradeTower     = new MyButton("Upgrade", 0, 0, 95, 20);

		// Set correct initial bounds so hit-testing works even before the first draw.
		updateTowerButtonBounds();

		towerButtons = new MyButton[3];

		int w = 50;
		int h = 50;
		int xStart = 110;
		int yStart = 650;
		int xOffset = (int) (w * 1.1f);

		for (int i = 0; i < towerButtons.length; i++)
			towerButtons[i] = new MyButton("", xStart + xOffset * i, yStart, w, h, i);
	}

	/**
	 * Single source of truth for the tower-popup button positions.
	 * Call this before ANY hit-testing (mouseMoved / mousePressed / mouseClicked)
	 * and at the start of drawDisplayedTower so drawing and hit-testing are always in sync.
	 */
	private void updateTowerButtonBounds() {
		bCloseTowerPopup.x      = TOWER_PANEL_X + 195;
		bCloseTowerPopup.y      = TOWER_PANEL_Y + 5;
		bCloseTowerPopup.width  = 20;
		bCloseTowerPopup.height = 20;

		sellTower.x      = TOWER_PANEL_X + 10;
		sellTower.y      = TOWER_PANEL_Y + 65;
		sellTower.width  = 95;
		sellTower.height = 20;

		upgradeTower.x      = TOWER_PANEL_X + 115;
		upgradeTower.y      = TOWER_PANEL_Y + 65;
		upgradeTower.width  = 95;
		upgradeTower.height = 20;
	}

	// Trigger game over with music
	public void removeOneLife() {
		lives--;
		if (lives <= 0) {
			playing.triggerGameOver();
		}
	}

	private void drawButtons(Graphics g) {
		bMenu.draw(g);
		bPause.draw(g);
		bLogout.draw(g);

		for (MyButton b : towerButtons) {
			drawTowerButton(g, b);
		}
	}

	private void drawTowerButton(Graphics g, MyButton b) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(new Color(0, 0, 0, 60));
		g2d.fillRoundRect(b.x + 3, b.y + 3, b.width, b.height, 12, 12);

		Color bgColor = b.isMouseOver() ? GRASS_GREEN : new Color(240, 240, 240);
		GradientPaint gradient = new GradientPaint(
				b.x, b.y, bgColor,
				b.x, b.y + b.height, darken(bgColor, 0.85f));
		g2d.setPaint(gradient);
		g2d.fillRoundRect(b.x, b.y, b.width, b.height, 12, 12);

		g2d.drawImage(playing.getTowerManager().getTowerImgs()[b.getId()],
				b.x + 5, b.y + 5, b.width - 10, b.height - 10, null);

		drawButtonFeedback(g, b);
	}

	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		updateAnimations();

		drawGradientBackground(g2d, x, y, width, height,
				new Color(230, 126, 34), new Color(200, 100, 20));

		g2d.setColor(GOLDEN_YELLOW);
		g2d.fillRect(x, y, width, 4);

		drawButtons(g);
		drawDisplayedTower(g2d);
		drawWaveInfoPanel(g2d);
		drawResourcesPanel(g2d);

		if (showTowerCost)
			drawTowerCost(g2d);

		if (playing.isGamePaused()) {
			drawPausedOverlay(g2d);
		}
	}

	private void updateAnimations() {
		animTick++;

		if (gold != lastGold) {
			goldAnimScale = 1.3f;
			lastGold = gold;
		}
		goldAnimScale += (1.0f - goldAnimScale) * 0.2f;
	}

	private void drawResourcesPanel(Graphics2D g2d) {
		drawPanel(g2d, 105, 710, 180, 80, LIGHT_CREAM);

		g2d.setFont(new Font("Arial", Font.BOLD, 18));

		int coinX = 115;
		int coinY = 725;
		int coinSize = (int) (20 * goldAnimScale);
		int coinOffset = (int) ((coinSize - 20) / 2);

		g2d.setColor(GOLDEN_YELLOW);
		g2d.fillOval(coinX - coinOffset, coinY - coinOffset, coinSize, coinSize);
		g2d.setColor(new Color(255, 255, 150));
		g2d.fillOval(coinX - coinOffset + 3, coinY - coinOffset + 3, coinSize - 10, coinSize - 10);
		g2d.setColor(DARK_BLUE);
		g2d.drawOval(coinX - coinOffset, coinY - coinOffset, coinSize, coinSize);

		g2d.setColor(GOLDEN_YELLOW);
		g2d.drawString("Gold:", 145, 730);
		g2d.setFont(new Font("Arial", Font.BOLD, 22));
		g2d.setColor(DARK_BLUE);
		g2d.drawString(gold + "g", 145, 755);

		g2d.setFont(new Font("Arial", Font.BOLD, 18));

		int heartX = 115;
		int heartY = 770;
		g2d.setColor(new Color(231, 76, 60));
		g2d.fillOval(heartX - 4, heartY - 2, 8, 8);
		g2d.fillOval(heartX + 4, heartY - 2, 8, 8);
		int[] xPoints = { heartX - 8, heartX, heartX + 8 };
		int[] yPoints = { heartY + 2, heartY + 12, heartY + 2 };
		g2d.fillPolygon(xPoints, yPoints, 3);

		Color livesColor = lives > 10 ? GRASS_GREEN : new Color(231, 76, 60);
		g2d.setColor(livesColor);
		g2d.drawString("Lives:", 145, 775);
		g2d.setFont(new Font("Arial", Font.BOLD, 22));
		g2d.drawString("" + lives, 220, 775);
	}

	private void drawWaveInfoPanel(Graphics2D g2d) {
		drawPanel(g2d, 420, 642, 210, 55, SKY_BLUE);

		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 16));

		int textY = 660;

		if (playing.getWaveManager().isWaveTimerStarted()) {
			float timeLeft = playing.getWaveManager().getTimeLeft();
			String formattedText = formatter.format(timeLeft);
			g2d.drawString("Next Wave: " + formattedText + "s", 430, textY);
		}

		int remaining = playing.getEnemyManger().getAmountOfAliveEnemies();
		g2d.drawString("Enemies: " + remaining, 430, textY + 20);

		int current = playing.getWaveManager().getWaveIndex();
		int size = playing.getWaveManager().getWaves().size();
		g2d.drawString("Wave " + (current + 1) + "/" + size, 530, textY + 20);
	}

	private void drawTowerCost(Graphics2D g2d) {
		drawPanel(g2d, 280, 650, 120, 60, new Color(255, 255, 255, 240));

		g2d.setColor(DARK_BLUE);
		g2d.setFont(new Font("Arial", Font.BOLD, 14));
		g2d.drawString(getTowerCostName(), 290, 670);

		g2d.setFont(new Font("Arial", Font.BOLD, 16));
		g2d.setColor(GOLDEN_YELLOW);
		g2d.drawString("Cost: " + getTowerCostCost() + "g", 290, 695);

		if (isTowerCostMoreThanCurrentGold()) {
			Color warningColor = new Color(200, 0, 0);

			g2d.setColor(new Color(255, 255, 200, 200));
			g2d.fillRoundRect(285, 705, 110, 25, 5, 5);

			g2d.setColor(warningColor);
			g2d.drawRoundRect(285, 705, 110, 25, 5, 5);

			g2d.setFont(new Font("Arial", Font.BOLD, 14));
			g2d.setColor(warningColor);
			g2d.drawString("NOT ENOUGH GOLD!", 288, 722);

			g2d.setColor(warningColor);
			g2d.setFont(new Font("Arial", Font.BOLD, 18));
			g2d.drawString("!", 300, 722);
		}
	}

	private boolean isTowerCostMoreThanCurrentGold() {
		return getTowerCostCost() > gold;
	}

	private String getTowerCostName() {
		return helpz.Constants.Towers.GetName(towerCostType);
	}

	private int getTowerCostCost() {
		return helpz.Constants.Towers.GetTowerCost(towerCostType);
	}

	private void drawDisplayedTower(Graphics2D g2d) {
		if (displayedTower != null) {
			// Sync button bounds before drawing so they always match what we hit-test.
			updateTowerButtonBounds();

			drawPanel(g2d, TOWER_PANEL_X, TOWER_PANEL_Y, 220, 90, new Color(245, 240, 220, 240));

			drawCloseButton(g2d, bCloseTowerPopup);

			g2d.setColor(GRASS_GREEN);
			g2d.fillRoundRect(TOWER_PANEL_X + 10, TOWER_PANEL_Y + 10, 50, 50, 8, 8);
			g2d.setColor(DARK_BLUE);
			g2d.drawRoundRect(TOWER_PANEL_X + 10, TOWER_PANEL_Y + 10, 50, 50, 8, 8);

			g2d.drawImage(playing.getTowerManager().getTowerImgs()[displayedTower.getTowerType()],
					TOWER_PANEL_X + 15, TOWER_PANEL_Y + 15, 40, 40, null);

			g2d.setFont(new Font("Arial", Font.BOLD, 15));
			g2d.setColor(DARK_BLUE);
			g2d.drawString(Towers.GetName(displayedTower.getTowerType()), TOWER_PANEL_X + 70, TOWER_PANEL_Y + 22);

			g2d.setFont(new Font("Arial", Font.PLAIN, 12));
			g2d.drawString("ID: " + displayedTower.getId(), TOWER_PANEL_X + 70, TOWER_PANEL_Y + 38);

			String tierStars = "★".repeat(displayedTower.getTier());
			g2d.setColor(GOLDEN_YELLOW);
			g2d.setFont(new Font("Arial", Font.BOLD, 16));
			g2d.drawString(tierStars, TOWER_PANEL_X + 150, TOWER_PANEL_Y + 25);

			sellTower.draw(g2d);

			if (displayedTower.getTier() < 3 && gold >= getUpgradeAmount(displayedTower)) {
				upgradeTower.draw(g2d);
			}

			if (sellTower.isMouseOver()) {
				g2d.setColor(new Color(231, 76, 60));
				g2d.setFont(new Font("Arial", Font.BOLD, 11));
				g2d.drawString("+" + getSellAmount(displayedTower) + "g",
						sellTower.x + 30, sellTower.y - 5);
			} else if (upgradeTower.isMouseOver() && gold >= getUpgradeAmount(displayedTower)) {
				g2d.setColor(SKY_BLUE);
				g2d.setFont(new Font("Arial", Font.BOLD, 11));
				g2d.drawString("-" + getUpgradeAmount(displayedTower) + "g",
						upgradeTower.x + 25, upgradeTower.y - 5);
			}

			drawDisplayedTowerRange(g2d);
			drawDisplayedTowerBorder(g2d);
		}
	}

	private void drawCloseButton(Graphics2D g2d, MyButton closeBtn) {
		g2d.setColor(new Color(0, 0, 0, 60));
		g2d.fillRoundRect(closeBtn.x + 1, closeBtn.y + 1, closeBtn.width, closeBtn.height, 10, 10);

		Color bgColor = closeBtn.isMouseOver() ? new Color(231, 76, 60) : new Color(200, 200, 200);
		GradientPaint gradient = new GradientPaint(
				closeBtn.x, closeBtn.y, bgColor,
				closeBtn.x, closeBtn.y + closeBtn.height, darken(bgColor, 0.85f));
		g2d.setPaint(gradient);
		g2d.fillRoundRect(closeBtn.x, closeBtn.y, closeBtn.width, closeBtn.height, 10, 10);

		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 14));
		g2d.drawString("×", closeBtn.x + 5, closeBtn.y + 16);

		g2d.setColor(closeBtn.isMouseOver() ? Color.WHITE : DARK_BLUE);
		g2d.drawRoundRect(closeBtn.x, closeBtn.y, closeBtn.width, closeBtn.height, 10, 10);
	}

	private int getUpgradeAmount(Tower displayedTower) {
		return (int) (helpz.Constants.Towers.GetTowerCost(displayedTower.getTowerType()) * 0.3f);
	}

	private int getSellAmount(Tower displayedTower) {
		int upgradeCost = (displayedTower.getTier() - 1) * getUpgradeAmount(displayedTower);
		upgradeCost *= 0.5f;
		return helpz.Constants.Towers.GetTowerCost(displayedTower.getTowerType()) / 2 + upgradeCost;
	}

	private void drawDisplayedTowerRange(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(124, 200, 80, 100));
		g2d.fillOval(displayedTower.getX() + 16 - (int) (displayedTower.getRange() * 2) / 2,
				displayedTower.getY() + 16 - (int) (displayedTower.getRange() * 2) / 2,
				(int) displayedTower.getRange() * 2, (int) displayedTower.getRange() * 2);

		g2d.setColor(GRASS_GREEN);
		g2d.drawOval(displayedTower.getX() + 16 - (int) (displayedTower.getRange() * 2) / 2,
				displayedTower.getY() + 16 - (int) (displayedTower.getRange() * 2) / 2,
				(int) displayedTower.getRange() * 2, (int) displayedTower.getRange() * 2);
	}

	private void drawDisplayedTowerBorder(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(GOLDEN_YELLOW);
		g2d.fillRoundRect(displayedTower.getX() - 2, displayedTower.getY() - 2, 36, 36, 6, 6);
		g2d.setColor(GRASS_GREEN);
		g2d.drawRoundRect(displayedTower.getX(), displayedTower.getY(), 32, 32, 4, 4);
	}

	private void drawPausedOverlay(Graphics2D g2d) {
		g2d.setColor(new Color(0, 0, 0, 100));
		g2d.fillRect(x, y, width, height);

		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 24));
		String pausedText = "GAME PAUSED";
		int textWidth = g2d.getFontMetrics().stringWidth(pausedText);
		g2d.drawString(pausedText, x + (width - textWidth) / 2, y + height / 2);
	}

	public void displayTower(Tower t) {
		displayedTower = t;
	}

	public void clearDisplayedTower() {
		displayedTower = null;
	}

	private void sellTowerClicked() {
		System.out.println("💰 Selling tower for " + getSellAmount(displayedTower) + " gold");

		playing.removeTower(displayedTower);
		gold += helpz.Constants.Towers.GetTowerCost(displayedTower.getTowerType()) / 2;

		int upgradeCost = (displayedTower.getTier() - 1) * getUpgradeAmount(displayedTower);
		upgradeCost *= 0.5f;
		gold += upgradeCost;

		displayedTower = null;
	}

	private void upgradeTowerClicked() {
		System.out.println("⬆️ Upgrading tower for " + getUpgradeAmount(displayedTower) + " gold");

		playing.upgradeTower(displayedTower);
		gold -= getUpgradeAmount(displayedTower);
	}

	private void togglePause() {
		playing.setGamePaused(!playing.isGamePaused());

		if (playing.isGamePaused())
			bPause.setText("Unpause");
		else
			bPause.setText("Pause");
	}

	private void logoutClicked() {
		int result = javax.swing.JOptionPane.showConfirmDialog(
				null,
				"Are you sure you want to logout? All unsaved progress will be lost.",
				"Confirm Logout",
				javax.swing.JOptionPane.YES_NO_OPTION,
				javax.swing.JOptionPane.QUESTION_MESSAGE);

		if (result == javax.swing.JOptionPane.YES_OPTION) {
			if (SessionManager.getInstance().isLoggedIn()) {
				System.out.println("Logging out user: " + SessionManager.getInstance().getUsername());
				SessionManager.getInstance().logout();
			}

			playing.resetEverything();
			setGameState(MENU);
		}
	}

	public void closeTowerPopupIfClickedElsewhere(int x, int y) {
		if (displayedTower != null) {
			updateTowerButtonBounds();
			if (!bCloseTowerPopup.getBounds().contains(x, y) &&
					!sellTower.getBounds().contains(x, y) &&
					!upgradeTower.getBounds().contains(x, y)) {
				Tower clickedTower = playing.getTowerManager().getTowerAt(x, y);
				if (clickedTower != displayedTower) {
					displayedTower = null;
				}
			}
		}
	}

	public void mouseClicked(int x, int y) {
		// Always sync button bounds before hit-testing.
		updateTowerButtonBounds();

		if (bMenu.getBounds().contains(x, y)) {
			setGameState(MENU);
		} else if (bPause.getBounds().contains(x, y)) {
			togglePause();
		} else if (bLogout.getBounds().contains(x, y)) {
			logoutClicked();
		} else if (displayedTower != null && bCloseTowerPopup.getBounds().contains(x, y)) {
			displayedTower = null;
			return;
		} else {
			if (displayedTower != null) {
				if (sellTower.getBounds().contains(x, y)) {
					sellTowerClicked();
					return;
				} else if (upgradeTower.getBounds().contains(x, y) && displayedTower.getTier() < 3
						&& gold >= getUpgradeAmount(displayedTower)) {
					upgradeTowerClicked();
					return;
				}
			}

			for (MyButton b : towerButtons) {
				if (b.getBounds().contains(x, y)) {
					if (!isGoldEnoughForTower(b.getId()))
						return;

					selectedTower = new Tower(0, 0, -1, b.getId());
					playing.setSelectedTower(selectedTower);
					return;
				}
			}
		}
	}

	private boolean isGoldEnoughForTower(int towerType) {
		return gold >= helpz.Constants.Towers.GetTowerCost(towerType);
	}

	public void mouseMoved(int x, int y) {
		// Always sync button bounds before hit-testing.
		updateTowerButtonBounds();

		bMenu.setMouseOver(false);
		bPause.setMouseOver(false);
		bLogout.setMouseOver(false);
		showTowerCost = false;
		sellTower.setMouseOver(false);
		upgradeTower.setMouseOver(false);

		if (displayedTower != null) {
			bCloseTowerPopup.setMouseOver(bCloseTowerPopup.getBounds().contains(x, y));
		}

		for (MyButton b : towerButtons)
			b.setMouseOver(false);

		if (bMenu.getBounds().contains(x, y)) {
			bMenu.setMouseOver(true);
		} else if (bPause.getBounds().contains(x, y)) {
			bPause.setMouseOver(true);
		} else if (bLogout.getBounds().contains(x, y)) {
			bLogout.setMouseOver(true);
		} else {
			if (displayedTower != null) {
				if (sellTower.getBounds().contains(x, y)) {
					sellTower.setMouseOver(true);
					return;
				} else if (upgradeTower.getBounds().contains(x, y) && displayedTower.getTier() < 3) {
					upgradeTower.setMouseOver(true);
					return;
				}
			}

			for (MyButton b : towerButtons) {
				if (b.getBounds().contains(x, y)) {
					b.setMouseOver(true);
					showTowerCost = true;
					towerCostType = b.getId();
					return;
				}
			}
		}
	}

	public void mousePressed(int x, int y) {
		// Always sync button bounds before hit-testing.
		updateTowerButtonBounds();

		if (bMenu.getBounds().contains(x, y)) {
			bMenu.setMousePressed(true);
		} else if (bPause.getBounds().contains(x, y)) {
			bPause.setMousePressed(true);
		} else if (bLogout.getBounds().contains(x, y)) {
			bLogout.setMousePressed(true);
		} else if (displayedTower != null && bCloseTowerPopup.getBounds().contains(x, y)) {
			bCloseTowerPopup.setMousePressed(true);
		} else {
			if (displayedTower != null) {
				if (sellTower.getBounds().contains(x, y)) {
					sellTower.setMousePressed(true);
					return;
				} else if (upgradeTower.getBounds().contains(x, y) && displayedTower.getTier() < 3) {
					upgradeTower.setMousePressed(true);
					return;
				}
			}
			for (MyButton b : towerButtons) {
				if (b.getBounds().contains(x, y)) {
					b.setMousePressed(true);
					return;
				}
			}
		}
	}

	public void mouseReleased(int x, int y) {
		bMenu.resetBooleans();
		bPause.resetBooleans();
		bLogout.resetBooleans();
		bCloseTowerPopup.resetBooleans();
		for (MyButton b : towerButtons)
			b.resetBooleans();
		sellTower.resetBooleans();
		upgradeTower.resetBooleans();
	}

	public void payForTower(int towerType) {
		this.gold -= helpz.Constants.Towers.GetTowerCost(towerType);
	}

	public void addGold(int getReward) {
		this.gold += getReward;
	}

	public int getLives() {
		return lives;
	}

	public int getGold() {
		return gold;
	}
}