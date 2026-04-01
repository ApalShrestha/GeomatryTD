package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static main.GameStates.*;

import main.Game;
import main.GameStates;

public class KeyboardListener implements KeyListener {
    private Game game;

    public KeyboardListener(Game game) {
        this.game = game;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (GameStates.gameState) {
            case EDIT:
                game.getEditor().keyPressed(e);
                break;
            case PLAYING:
                game.getPlaying().keyPressed(e);
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
            case MENU:
                game.getMenu().keyPressed(e);
                break;
            default:
                // ESC always available to go back to menu
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
}