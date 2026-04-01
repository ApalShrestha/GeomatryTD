package main;

import managers.SessionManager;

public enum GameStates {

    MENU,
    STORY,
    LOGIN,
    REGISTER,
    LEVEL_SELECT,
    PLAYING,
    ADMIN_PANEL,
    GAME_OVER,
    VICTORY,
    SETTINGS,
    EDIT;

    public static Game game;
    public static GameStates gameState = MENU; // Start with STORY instead of LOGIN

    public static void setGameState(GameStates state) {
        // 1. Login checks (prevent logged-in users from going to login/register)
        if (state == LOGIN && SessionManager.getInstance().isLoggedIn()) {
            System.out.println("User already logged in. Redirecting to MENU.");
            gameState = MENU;
            if (game != null)
                game.repaint();
            return;
        }
        if (state == REGISTER && SessionManager.getInstance().isLoggedIn()) {
            System.out.println("User already logged in. Cannot register.");
            gameState = MENU;
            if (game != null)
                game.repaint();
            return;
        }

        // 2. Set the new state
        gameState = state;

        // 3. Level select refresh
        if (state == LEVEL_SELECT && game != null) {
            game.getLevelSelect().refreshLevels();
        }

        // 4. Focus and repaint
        if (game != null) {
            if (game.getGameScreen() != null) {
                game.getGameScreen().requestFocusInWindow();
            }
            game.repaint();
        }
    }
}