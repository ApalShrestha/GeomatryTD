package main;

import java.awt.Graphics;
import scenes.*;

public class Render {

	private Game game;

	public Render(Game game) {
		this.game = game;
	}

	public void render(Graphics g) {

		switch (GameStates.gameState) {
		case MENU:
			game.getMenu().render(g);
			break;
		case STORY:
			game.getStory().render(g);
			break;
		case PLAYING:
			game.getPlaying().render(g);
			break;
		case SETTINGS:
			game.getSettings().render(g);
			break;
		case EDIT:
			game.getEditor().render(g);
			break;
		case GAME_OVER:
			game.getGameOver().render(g);
			break;
		case VICTORY:
			game.getVictory().render(g);
			break;
		case LEVEL_SELECT:
			game.getLevelSelect().render(g);
			break;
		case LOGIN:
			game.getLoginScene().render(g);
			break;
		case REGISTER:
			game.getRegisterScene().render(g);
			break;
		case ADMIN_PANEL:  // ADD THIS
			game.getAdminPanel().render(g);
			break;
		}
	}
}