package admin;

import static main.GameStates.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import main.*;
import managers.SessionManager;
import scenes.*;
/**
 * Main Admin Panel - Acts as controller for all admin views
 * Manages navigation between Dashboard, Leaderboard, Players, Levels, and Map Editor
 */
public class AdminPanel extends GameScene implements SceneMethods {
    
    // View modes
    private enum ViewMode { 
        DASHBOARD, 
        LEADERBOARD, 
        PLAYER_MANAGER, 
        LEVEL_MANAGER,
        MAP_EDITOR 
    }
    
    private ViewMode currentView;
    
    // View components
    private AdminDashboard dashboard;
    private LeaderboardViewer leaderboard;
    private PlayerManager playerManager;
    private LevelManager levelManager;
    private MapEditor mapEditor;
    
    public AdminPanel(Game game) {
        super(game);
        initComponents();
        currentView = ViewMode.DASHBOARD;
    }
    
    private void initComponents() {
        dashboard = new AdminDashboard();
        leaderboard = new LeaderboardViewer();
        playerManager = new PlayerManager();
        levelManager = new LevelManager();
        mapEditor = new MapEditor(game);
        
        // Set callback for level editing
        levelManager.setEditCallback(new LevelManager.LevelEditCallback() {
            @Override
            public void onEditLevel(int levelId) {
                if (levelId == -1) {
                    // Create new level
                    mapEditor.createNewLevel();
                } else {
                    // Edit existing level
                    mapEditor.loadLevel(levelId);
                }
                currentView = ViewMode.MAP_EDITOR;
            }
        });
    }
    
    
    public void update(){
        updateTick();
    }
    
    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Render based on current view
        switch (currentView) {
            case DASHBOARD:
                dashboard.render(g);
                break;
            case LEADERBOARD:
                leaderboard.render(g);
                break;
            case PLAYER_MANAGER:
                playerManager.render(g);
                break;
            case LEVEL_MANAGER:
                levelManager.render(g2d);
                break;
            case MAP_EDITOR:
                mapEditor.render(g);
                break;
        }
    }
    
    @Override
    public void mouseClicked(int x, int y) {
        switch (currentView) {
            case DASHBOARD:
                handleDashboardClick(x, y);
                break;
            case LEADERBOARD:
                if (leaderboard.getBackButton().getBounds().contains(x, y)) {
                    currentView = ViewMode.DASHBOARD;
                } else {
                    leaderboard.handleClick(x, y);
                }
                break;
            case PLAYER_MANAGER:
                if (playerManager.getBackButton().getBounds().contains(x, y)) {
                    currentView = ViewMode.DASHBOARD;
                    dashboard.refreshStats();
                } else {
                    playerManager.handleClick(x, y);
                }
                break;
            case LEVEL_MANAGER:
                if (levelManager.getBackButton().getBounds().contains(x, y)) {
                    currentView = ViewMode.DASHBOARD;
                    dashboard.refreshStats();
                } else {
                    levelManager.handleClick(x, y);
                }
                break;
            case MAP_EDITOR:
                mapEditor.mouseClicked(x, y);
                break;
        }
    }
    
    private void handleDashboardClick(int x, int y) {
        if (dashboard.getMapEditorButton().getBounds().contains(x, y)) {
            // Go to map editor to create new level
            mapEditor.createNewLevel();
            currentView = ViewMode.MAP_EDITOR;
        } else if (dashboard.getLeaderboardButton().getBounds().contains(x, y)) {
            currentView = ViewMode.LEADERBOARD;
            leaderboard.refresh();
        } else if (dashboard.getPlayerManagerButton().getBounds().contains(x, y)) {
            currentView = ViewMode.PLAYER_MANAGER;
            playerManager.refresh();
        } else if (dashboard.getLevelManagerButton().getBounds().contains(x, y)) {
            currentView = ViewMode.LEVEL_MANAGER;
            levelManager.refresh();
        } else if (dashboard.getLogoutButton().getBounds().contains(x, y)) {
            SessionManager.getInstance().logout();
            setGameState(MENU);
        }
    }
    
    @Override
    public void mouseMoved(int x, int y) {
        switch (currentView) {
            case DASHBOARD:
                updateDashboardHover(x, y);
                break;
            case LEADERBOARD:
                leaderboard.updateHover(x, y);
                break;
            case PLAYER_MANAGER:
                playerManager.updateHover(x, y);
                break;
            case LEVEL_MANAGER:
                levelManager.updateHover(x, y);
                break;
            case MAP_EDITOR:
                mapEditor.mouseMoved(x, y);
                break;
        }
    }
    
    private void updateDashboardHover(int x, int y) {
        dashboard.getMapEditorButton().setMouseOver(false);
        dashboard.getLeaderboardButton().setMouseOver(false);
        dashboard.getPlayerManagerButton().setMouseOver(false);
        dashboard.getLevelManagerButton().setMouseOver(false);
        dashboard.getLogoutButton().setMouseOver(false);
        
        if (dashboard.getMapEditorButton().getBounds().contains(x, y))
            dashboard.getMapEditorButton().setMouseOver(true);
        else if (dashboard.getLeaderboardButton().getBounds().contains(x, y))
            dashboard.getLeaderboardButton().setMouseOver(true);
        else if (dashboard.getPlayerManagerButton().getBounds().contains(x, y))
            dashboard.getPlayerManagerButton().setMouseOver(true);
        else if (dashboard.getLevelManagerButton().getBounds().contains(x, y))
            dashboard.getLevelManagerButton().setMouseOver(true);
        else if (dashboard.getLogoutButton().getBounds().contains(x, y))
            dashboard.getLogoutButton().setMouseOver(true);
    }
    
    @Override
    public void mousePressed(int x, int y) {
        if (currentView == ViewMode.DASHBOARD) {
            if (dashboard.getMapEditorButton().getBounds().contains(x, y))
                dashboard.getMapEditorButton().setMousePressed(true);
            else if (dashboard.getLeaderboardButton().getBounds().contains(x, y))
                dashboard.getLeaderboardButton().setMousePressed(true);
            else if (dashboard.getPlayerManagerButton().getBounds().contains(x, y))
                dashboard.getPlayerManagerButton().setMousePressed(true);
            else if (dashboard.getLevelManagerButton().getBounds().contains(x, y))
                dashboard.getLevelManagerButton().setMousePressed(true);
            else if (dashboard.getLogoutButton().getBounds().contains(x, y))
                dashboard.getLogoutButton().setMousePressed(true);
        } else if (currentView == ViewMode.MAP_EDITOR) {
            mapEditor.mousePressed(x, y);
        }
    }
    
    @Override
    public void mouseReleased(int x, int y) {
        if (currentView == ViewMode.DASHBOARD) {
            dashboard.getMapEditorButton().resetBooleans();
            dashboard.getLeaderboardButton().resetBooleans();
            dashboard.getPlayerManagerButton().resetBooleans();
            dashboard.getLevelManagerButton().resetBooleans();
            dashboard.getLogoutButton().resetBooleans();
        } else if (currentView == ViewMode.MAP_EDITOR) {
            mapEditor.mouseReleased(x, y);
        }
    }
    
    @Override
    public void mouseDragged(int x, int y) {
        if (currentView == ViewMode.MAP_EDITOR) {
            mapEditor.mouseDragged(x, y);
        }
    }
    
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (currentView == ViewMode.MAP_EDITOR) {
            // Let map editor handle keys first
            if (mapEditor.isInputting()) {
                mapEditor.keyPressed(e);
                return;
            }
            
            // ESC from map editor returns to dashboard
            if (key == KeyEvent.VK_ESCAPE) {
                currentView = ViewMode.DASHBOARD;
                dashboard.refreshStats();
                return;
            }
            
            mapEditor.keyPressed(e);
        } else {
            // Other views
            if (key == KeyEvent.VK_ESCAPE) {
                if (currentView == ViewMode.DASHBOARD) {
                    setGameState(MENU);
                } else {
                    currentView = ViewMode.DASHBOARD;
                    dashboard.refreshStats();
                }
            } else if (key == KeyEvent.VK_UP) {
                handleScrollUp();
            } else if (key == KeyEvent.VK_DOWN) {
                handleScrollDown();
            }
        }
    }
    
    private void handleScrollUp() {
        switch (currentView) {
            case LEADERBOARD:
                leaderboard.scrollUp();
                break;
            case PLAYER_MANAGER:
                playerManager.scrollUp();
                break;
            case LEVEL_MANAGER:
                levelManager.scrollUp();
                break;
        }
    }
    
    private void handleScrollDown() {
        switch (currentView) {
            case LEADERBOARD:
                leaderboard.scrollDown();
                break;
            case PLAYER_MANAGER:
                playerManager.scrollDown();
                break;
            case LEVEL_MANAGER:
                levelManager.scrollDown();
                break;
        }
    }
    
    /**
     * Return to dashboard view
     */
    public void returnToDashboard() {
        currentView = ViewMode.DASHBOARD;
        dashboard.refreshStats();
    }
    
    /**
     * Get current view mode
     */
    public String getCurrentViewName() {
        return currentView.name();
    }
}