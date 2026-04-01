package main;

import javax.swing.JFrame;

import managers.TileManager;
import scenes.*;
import admin.AdminPanel;
import static main.GameStates.*;

public class Game extends JFrame implements Runnable {

	private GameScreen gameScreen;
	private Thread gameThread;
	private GameStates gameStates;

	private final double FPS_SET = 120.0;
	private final double UPS_SET = 60.0;

	// Classes
	private Render render;
	private Menu menu;
	private Story story;
	private Playing playing;
	private Settings settings;
	private Editing editing;
	private GameOver gameOver;
	private Victory victory;
	private LevelSelect levelSelect;
	private LoginScene loginScene;
	private RegisterScene registerScene;
	private AdminPanel adminPanel;
	private TileManager tileManager;

	// PERFORMANCE: Track if we actually need to repaint
	private volatile boolean needsRepaint = true;

	public Game() {
		initClasses();

		GameStates.game = this;

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("GeoMatry - Tower Defense");
		add(gameScreen);
		pack();
		setVisible(true);
	}

	private void initClasses() {
		tileManager = new TileManager();
		render = new Render(this);
		gameScreen = new GameScreen(this);
		menu = new Menu(this);
		story = new Story(this);
		playing = new Playing(this);
		settings = new Settings(this);
		editing = new Editing(this);
		gameOver = new GameOver(this);
		victory = new Victory(this);
		levelSelect = new LevelSelect(this);
		loginScene = new LoginScene(this);
		registerScene = new RegisterScene(this);
		adminPanel = new AdminPanel(this);
	}

	private void start() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	private void updateGame() {
		switch (GameStates.gameState) {
			case EDIT:
				editing.update();
				break;
			case MENU:
				menu.update();
				break;
			case STORY:
				story.update();
				break;
			case PLAYING:
				playing.update();
				break;
			case SETTINGS:
				break;
			case GAME_OVER:
				break;
			case VICTORY:
				break;
			case LEVEL_SELECT:
				levelSelect.update();
				break;
			case LOGIN:
				break;
			case REGISTER:
				break;
			case ADMIN_PANEL:
				adminPanel.update();
				break;
			default:
				break;
		}
	}

	public static void main(String[] args) {
		Game game = new Game();
		game.gameScreen.initInputs();
		game.start();
	}

	@Override
	public void run() {
		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long lastFrame = System.nanoTime();
		long lastUpdate = System.nanoTime();
		long lastTimeCheck = System.currentTimeMillis();

		int frames = 0;
		int updates = 0;

		long now;

		while (true) {
			now = System.nanoTime();

			// FIXED: Only render when enough time has passed
			if (now - lastFrame >= timePerFrame) {
				// FIXED: Use SwingUtilities for thread-safe rendering
				if (needsRepaint) {
					gameScreen.repaint();
					needsRepaint = false;
				}
				lastFrame = now;
				frames++;
			}

			// Update at fixed rate
			if (now - lastUpdate >= timePerUpdate) {
				updateGame();
				needsRepaint = true; // Mark that we need to repaint after update
				lastUpdate = now;
				updates++;
			}

			// FPS counter (optional)
			if (System.currentTimeMillis() - lastTimeCheck >= 1000) {
				// Uncomment to see FPS/UPS
				// System.out.println("FPS: " + frames + " | UPS: " + updates);
				frames = 0;
				updates = 0;
				lastTimeCheck = System.currentTimeMillis();
			}

			// PERFORMANCE: Sleep briefly to prevent CPU spin
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	/**
	 * Request a repaint on next frame
	 */
	public void requestRepaint() {
		needsRepaint = true;
	}

	public void setGameState(GameStates state) {
		GameStates.setGameState(state);
		this.gameStates = state;
		requestRepaint(); // Ensure we repaint when state changes
	}

	// Getters
	public GameStates getGameState() {
		return GameStates.gameState;
	}

	public GameScreen getGameScreen() {
		return gameScreen;
	}

	public Render getRender() {
		return render;
	}

	public Menu getMenu() {
		return menu;
	}

	public Playing getPlaying() {
		return playing;
	}

	public Settings getSettings() {
		return settings;
	}

	public Editing getEditor() {
		return editing;
	}

	public GameOver getGameOver() {
		return gameOver;
	}

	public TileManager getTileManager() {
		return tileManager;
	}

	public Story getStory() {
		return story;
	}

	public Victory getVictory() {
		return victory;
	}

	public LevelSelect getLevelSelect() {
		return levelSelect;
	}

	public LoginScene getLoginScene() {
		return loginScene;
	}

	public RegisterScene getRegisterScene() {
		return registerScene;
	}

	public AdminPanel getAdminPanel() {
		return adminPanel;
	}
}