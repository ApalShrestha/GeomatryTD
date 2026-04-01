package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/**c
 * PERFORMANCE-OPTIMIZED GameScreen
 * - Removed all console spam (System.out.println)
 * - Proper focus management
 * - Clean event routing
 */
public class GameScreen extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

	private Game game;
	private Dimension size;

	public GameScreen(Game game) {
		this.game = game;
		setPanelSize();

		setFocusable(true);
		setRequestFocusEnabled(true);
	}

	public void initInputs() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		requestFocus();
		setFocusable(true);
	}

	private void setPanelSize() {
		size = new Dimension(640, 800);
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		game.getRender().render(g);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		requestFocusInWindow();
		
		switch (GameStates.gameState) {
			case PLAYING:
				game.getPlaying().keyPressed(e);
				break;
			case EDIT:
				game.getEditor().keyPressed(e);
				break;
			case LEVEL_SELECT:
				game.getLevelSelect().keyPressed(e);
				break;
			case STORY:
				game.getStory().keyPressed(e);
				break;
			case LOGIN:
				game.getLoginScene().keyPressed(e);
				break;
			case REGISTER:
				game.getRegisterScene().keyPressed(e);
				break;
			case ADMIN_PANEL:
				game.getAdminPanel().keyPressed(e);
				break;
			default:
				// ESC always available to go back
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					if (GameStates.gameState != GameStates.MENU) {
						GameStates.setGameState(GameStates.MENU);
					}
				}
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Not used currently
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Not used currently
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
		
		if (e.getButton() == MouseEvent.BUTTON1) {
			switch (GameStates.gameState) {
				case MENU:
					game.getMenu().mouseClicked(e.getX(), e.getY());
					break;
				case PLAYING:
					game.getPlaying().mouseClicked(e.getX(), e.getY());
					break;
				case SETTINGS:
					game.getSettings().mouseClicked(e.getX(), e.getY());
					break;
				case EDIT:
					game.getEditor().mouseClicked(e.getX(), e.getY());
					break;
				case GAME_OVER:
					game.getGameOver().mouseClicked(e.getX(), e.getY());
					break;
				case STORY:
					game.getStory().mouseClicked(e.getX(), e.getY());
					break;
				case VICTORY:
					game.getVictory().mouseClicked(e.getX(), e.getY());
					break;
				case LEVEL_SELECT:
					game.getLevelSelect().mouseClicked(e.getX(), e.getY());
					break;
				case LOGIN:
					game.getLoginScene().mouseClicked(e.getX(), e.getY());
					break;
				case REGISTER:
					game.getRegisterScene().mouseClicked(e.getX(), e.getY());
					break;
				case ADMIN_PANEL:
					game.getAdminPanel().mouseClicked(e.getX(), e.getY());
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		requestFocusInWindow();
		
		switch (GameStates.gameState) {
			case MENU:
				game.getMenu().mousePressed(e.getX(), e.getY());
				break;
			case PLAYING:
				game.getPlaying().mousePressed(e.getX(), e.getY());
				break;
			case SETTINGS:
				game.getSettings().mousePressed(e.getX(), e.getY());
				break;
			case EDIT:
				game.getEditor().mousePressed(e.getX(), e.getY());
				break;
			case GAME_OVER:
				game.getGameOver().mousePressed(e.getX(), e.getY());
				break;
			case STORY:
				game.getStory().mousePressed(e.getX(), e.getY());
				break;
			case VICTORY:
				game.getVictory().mousePressed(e.getX(), e.getY());
				break;
			case LEVEL_SELECT:
				game.getLevelSelect().mousePressed(e.getX(), e.getY());
				break;
			case LOGIN:
				game.getLoginScene().mousePressed(e.getX(), e.getY());
				break;
			case REGISTER:
				game.getRegisterScene().mousePressed(e.getX(), e.getY());
				break;
			case ADMIN_PANEL:
				game.getAdminPanel().mousePressed(e.getX(), e.getY());
				break;
			default:
				break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		requestFocusInWindow();
		
		switch (GameStates.gameState) {
			case MENU:
				game.getMenu().mouseReleased(e.getX(), e.getY());
				break;
			case PLAYING:
				game.getPlaying().mouseReleased(e.getX(), e.getY());
				break;
			case SETTINGS:
				game.getSettings().mouseReleased(e.getX(), e.getY());
				break;
			case EDIT:
				game.getEditor().mouseReleased(e.getX(), e.getY());
				break;
			case GAME_OVER:
				game.getGameOver().mouseReleased(e.getX(), e.getY());
				break;
			case STORY:
				game.getStory().mouseReleased(e.getX(), e.getY());
				break;
			case VICTORY:
				game.getVictory().mouseReleased(e.getX(), e.getY());
				break;
			case LEVEL_SELECT:
				game.getLevelSelect().mouseReleased(e.getX(), e.getY());
				break;
			case LOGIN:
				game.getLoginScene().mouseReleased(e.getX(), e.getY());
				break;
			case REGISTER:
				game.getRegisterScene().mouseReleased(e.getX(), e.getY());
				break;
			case ADMIN_PANEL:
				game.getAdminPanel().mouseReleased(e.getX(), e.getY());
				break;
			default:
				break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Not used
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Not used
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		requestFocusInWindow();
		
		switch (GameStates.gameState) {
			case MENU:
				game.getMenu().mouseDragged(e.getX(), e.getY());
				break;
			case PLAYING:
				game.getPlaying().mouseDragged(e.getX(), e.getY());
				break;
			case SETTINGS:
				game.getSettings().mouseDragged(e.getX(), e.getY());
				break;
			case EDIT:
				game.getEditor().mouseDragged(e.getX(), e.getY());
				break;
			case GAME_OVER:
				game.getGameOver().mouseDragged(e.getX(), e.getY());
				break;
			case STORY:
				game.getStory().mouseDragged(e.getX(), e.getY());
				break;
			case VICTORY:
				game.getVictory().mouseDragged(e.getX(), e.getY());
				break;
			case LEVEL_SELECT:
				game.getLevelSelect().mouseDragged(e.getX(), e.getY());
				break;
			case LOGIN:
				game.getLoginScene().mouseDragged(e.getX(), e.getY());
				break;
			case REGISTER:
				game.getRegisterScene().mouseDragged(e.getX(), e.getY());
				break;
			case ADMIN_PANEL:
				game.getAdminPanel().mouseDragged(e.getX(), e.getY());
				break;
			default:
				break;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		requestFocusInWindow();
		
		switch (GameStates.gameState) {
			case MENU:
				game.getMenu().mouseMoved(e.getX(), e.getY());
				break;
			case PLAYING:
				game.getPlaying().mouseMoved(e.getX(), e.getY());
				break;
			case SETTINGS:
				game.getSettings().mouseMoved(e.getX(), e.getY());
				break;
			case EDIT:
				game.getEditor().mouseMoved(e.getX(), e.getY());
				break;
			case GAME_OVER:
				game.getGameOver().mouseMoved(e.getX(), e.getY());
				break;
			case STORY:
				game.getStory().mouseMoved(e.getX(), e.getY());
				break;
			case VICTORY:
				game.getVictory().mouseMoved(e.getX(), e.getY());
				break;
			case LEVEL_SELECT:
				game.getLevelSelect().mouseMoved(e.getX(), e.getY());
				break;
			case LOGIN:
				game.getLoginScene().mouseMoved(e.getX(), e.getY());
				break;
			case REGISTER:
				game.getRegisterScene().mouseMoved(e.getX(), e.getY());
				break;
			case ADMIN_PANEL:
				game.getAdminPanel().mouseMoved(e.getX(), e.getY());
				break;
			default:
				break;
		}
	}
}