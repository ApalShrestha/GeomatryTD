package scenes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import helpz.LoadSave;
import main.Game;
import objects.PathPoint;
import objects.Tile;
import ui.Toolbar;

import static helpz.Constants.Tiles.ROAD_TILE;
import static main.GameStates.*;

/**
 * Map editor scene - ONLY accessible in development mode
 * Players cannot edit procedurally generated levels
 */
public class Editing extends GameScene implements SceneMethods {

	private int[][] lvl;
	private Tile selectedTile;
	private int mouseX, mouseY;
	private int lastTileX, lastTileY, lastTileId;
	private boolean drawSelect;
	private Toolbar toolbar;
	private PathPoint start, end;
	
	// Editor lock - set to true to prevent players from editing
	private boolean editorLocked = false;
	private String editorPassword = "dev123"; // Simple password for demo

	public Editing(Game game) {
		super(game);
		loadDefaultLevel();
		toolbar = new Toolbar(0, 640, 640, 160, this);
		
		// Lock editor for players - only allow editing in dev mode
		editorLocked = true;
	}

	private void loadDefaultLevel() {
		lvl = LoadSave.GetLevelData("new_level");
		ArrayList<PathPoint> points = LoadSave.GetLevelPathPoints("new_level");
		start = points.get(0);
		end = points.get(1);
	}

	public void update() {
		updateTick();
	}

	@Override
	public void render(Graphics g) {
		if (editorLocked) {
			drawLockedScreen(g);
			return;
		}

		drawLevel(g);
		toolbar.draw(g);
		drawSelectedTile(g);
		drawPathPoints(g);
	}
	
	/**
	 * Draw locked screen to prevent player editing
	 */
	private void drawLockedScreen(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Background
		g2d.setColor(new Color(50, 50, 50));
		g2d.fillRect(0, 0, 640, 800);
		
		// Lock icon
		int lockX = 280;
		int lockY = 250;
		int lockSize = 80;
		
		// Lock body
		g2d.setColor(new Color(200, 200, 200));
		g2d.fillRoundRect(lockX, lockY + 40, lockSize, lockSize, 15, 15);
		
		// Lock shackle
		g2d.setColor(new Color(200, 200, 200));
		for (int i = 0; i < 8; i++) {
			g2d.drawArc(lockX + 10 - i, lockY - i, lockSize - 20 + i * 2, lockSize, 0, 180);
		}
		
		// Keyhole
		g2d.setColor(new Color(80, 80, 80));
		g2d.fillOval(lockX + lockSize / 2 - 10, lockY + 60, 20, 20);
		g2d.fillRect(lockX + lockSize / 2 - 5, lockY + 75, 10, 25);
		
		// Text
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 36));
		String title = "EDITOR LOCKED";
		int titleWidth = g2d.getFontMetrics().stringWidth(title);
		g2d.drawString(title, 320 - titleWidth / 2, 400);
		
		g2d.setFont(new Font("Arial", Font.PLAIN, 18));
		g2d.setColor(new Color(200, 200, 200));
		String msg1 = "Map editing is disabled in gameplay mode.";
		String msg2 = "Levels are procedurally generated!";
		String msg3 = "Press ESC to return to menu.";
		
		int msg1Width = g2d.getFontMetrics().stringWidth(msg1);
		int msg2Width = g2d.getFontMetrics().stringWidth(msg2);
		int msg3Width = g2d.getFontMetrics().stringWidth(msg3);
		
		g2d.drawString(msg1, 320 - msg1Width / 2, 450);
		g2d.drawString(msg2, 320 - msg2Width / 2, 480);
		
		g2d.setColor(new Color(255, 215, 0));
		g2d.setFont(new Font("Arial", Font.BOLD, 16));
		g2d.drawString(msg3, 320 - msg3Width / 2, 530);
		
		// Dev note
		g2d.setColor(new Color(100, 100, 100));
		g2d.setFont(new Font("Arial", Font.ITALIC, 12));
		String devNote = "(Developer: Set editorLocked = false in Editing.java to enable)";
		int devWidth = g2d.getFontMetrics().stringWidth(devNote);
		g2d.drawString(devNote, 320 - devWidth / 2, 750);
	}

	private void drawPathPoints(Graphics g) {
		if (start != null)
			g.drawImage(toolbar.getStartPathImg(), start.getxCord() * 32, start.getyCord() * 32, 32, 32, null);

		if (end != null)
			g.drawImage(toolbar.getEndPathImg(), end.getxCord() * 32, end.getyCord() * 32, 32, 32, null);
	}

	private void drawLevel(Graphics g) {
		for (int y = 0; y < lvl.length; y++) {
			for (int x = 0; x < lvl[y].length; x++) {
				int id = lvl[y][x];
				if (isAnimation(id)) {
					g.drawImage(getSprite(id, animationIndex), x * 32, y * 32, null);
				} else
					g.drawImage(getSprite(id), x * 32, y * 32, null);
			}
		}
	}

	private void drawSelectedTile(Graphics g) {
		if (selectedTile != null && drawSelect) {
			g.drawImage(selectedTile.getSprite(), mouseX, mouseY, 32, 32, null);
		}
	}

	public void saveLevel() {
		if (editorLocked) {
			System.out.println("Cannot save - editor is locked!");
			return;
		}
		LoadSave.SaveLevel("new_level", lvl, start, end);
		game.getPlaying().setLevel(lvl);
	}

	public void setSelectedTile(Tile tile) {
		if (editorLocked) return;
		this.selectedTile = tile;
		drawSelect = true;
	}

	private void changeTile(int x, int y) {
		if (editorLocked) return;
		
		if (selectedTile != null) {
			int tileX = x / 32;
			int tileY = y / 32;

			if (selectedTile.getId() >= 0) {
				if (lastTileX == tileX && lastTileY == tileY && lastTileId == selectedTile.getId())
					return;

				lastTileX = tileX;
				lastTileY = tileY;
				lastTileId = selectedTile.getId();

				lvl[tileY][tileX] = selectedTile.getId();
			} else {
				int id = lvl[tileY][tileX];
				if (game.getTileManager().getTile(id).getTileType() == ROAD_TILE) {
					if (selectedTile.getId() == -1)
						start = new PathPoint(tileX, tileY);
					else
						end = new PathPoint(tileX, tileY);
				}
			}
		}
	}

	@Override
	public void mouseClicked(int x, int y) {
		if (editorLocked) return;
		
		if (y >= 640) {
			toolbar.mouseClicked(x, y);
		} else {
			changeTile(mouseX, mouseY);
		}
	}

	@Override
	public void mouseMoved(int x, int y) {
		if (editorLocked) {
			drawSelect = false;
			return;
		}

		if (y >= 640) {
			toolbar.mouseMoved(x, y);
			drawSelect = false;
		} else {
			drawSelect = true;
			mouseX = (x / 32) * 32;
			mouseY = (y / 32) * 32;
		}
	}

	@Override
	public void mousePressed(int x, int y) {
		if (editorLocked) return;
		
		if (y >= 640)
			toolbar.mousePressed(x, y);
	}

	@Override
	public void mouseReleased(int x, int y) {
		if (editorLocked) return;
		toolbar.mouseReleased(x, y);
	}

	@Override
	public void mouseDragged(int x, int y) {
		if (editorLocked) return;
		
		if (y >= 640) {
			// Do nothing in toolbar
		} else {
			changeTile(x, y);
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setGameState(MENU);
		}
		
		if (editorLocked) return;
		
		if (e.getKeyCode() == KeyEvent.VK_R)
			toolbar.rotateSprite();
	}
	
	/**
	 * Method to unlock editor (for development)
	 */
	public void unlockEditor(String password) {
		if (password.equals(editorPassword)) {
			editorLocked = false;
			System.out.println("Editor unlocked!");
		}
	}
	
	/**
	 * Public method to check if editor is locked
	 */
	public boolean isEditorLocked() {
		return editorLocked;
	}
}