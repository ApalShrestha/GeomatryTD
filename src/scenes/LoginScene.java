package scenes;

import static main.GameStates.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import main.Game;
import main.GameStates;
import managers.DatabaseManager;
import managers.SessionManager;
import ui.MyButton;

public class LoginScene extends GameScene implements SceneMethods {

    private MyButton bLogin, bRegister, bBack;
    private String username = "";
    private String password = "";
    private String message = "";
    private Color messageColor = Color.RED;
    private int activeField = 0;
    private BufferedImage backgroundImg;
    
    private boolean isProcessing = false;

    public LoginScene(Game game) {
        super(game);
        loadBackground();
        initButtons();
    }
    
    private void loadBackground() {
        try {
            File bgFile = new File("res/warchatgpt.png");
            if (bgFile.exists()) {
                backgroundImg = ImageIO.read(bgFile);
            }
        } catch (Exception e) {
            // Silent fallback
        }
    }

    private void initButtons() {
        int w = 200, h = 50, x = 220;
        bLogin = new MyButton("LOGIN", x, 320, w, h);
        bRegister = new MyButton("Create Account", x, 390, w, h);
        bBack = new MyButton("Back", x, 460, w, h);
    }

    @Override
    public void render(Graphics g) {
        if (SessionManager.getInstance().isLoggedIn() && !isProcessing) {
            isProcessing = true;
            GameStates.setGameState(MENU);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        if (backgroundImg != null) {
            g2d.drawImage(backgroundImg, 0, 0, 640, 800, null);
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(0, 0, 640, 800);
        } else {
            g2d.setColor(new Color(70, 130, 180));
            g2d.fillRect(0, 0, 640, 800);
        }

        // Title
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        int titleWidth = g2d.getFontMetrics().stringWidth("LOGIN");
        g2d.drawString("LOGIN", 320 - titleWidth/2, 100);

        // Fields - FIX: Pass field type (1 for username, 2 for password)
        drawField(g, "Username:", username, 150, 180, activeField == 1, 1);
        drawField(g, "Password:", password, 150, 250, activeField == 2, 2);

        // Message with color
        if (!message.isEmpty()) {
            g2d.setColor(messageColor);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            int msgWidth = g2d.getFontMetrics().stringWidth(message);
            g2d.drawString(message, 320 - msgWidth/2, 380);
        }

        // Buttons
        bLogin.draw(g);
        bRegister.draw(g);
        bBack.draw(g);
    }

    // FIX: Added fieldType parameter to distinguish between username and password fields
    private void drawField(Graphics g, String label, String value, int x, int y, boolean active, int fieldType) {
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(active ? Color.WHITE : new Color(220, 220, 220));
        g2d.fillRect(x, y, 340, 40);
        g2d.setColor(active ? Color.YELLOW : Color.CYAN);
        g2d.drawRect(x, y, 340, 40);

        g2d.setColor(active ? Color.YELLOW : Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(label, x, y - 8);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // FIX: Use fieldType to determine if this is password field (fieldType == 2)
        String display = (fieldType == 2) ? "*".repeat(value.length()) : value;
        g2d.drawString(display + (active ? "_" : ""), x + 10, y + 28);
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (isProcessing) return;
        
        if (SessionManager.getInstance().isLoggedIn()) {
            GameStates.setGameState(MENU);
            return;
        }
        
        if (y >= 180 && y <= 220)
            activeField = 1;
        else if (y >= 250 && y <= 290)
            activeField = 2;
        else
            activeField = 0;

        if (bLogin.getBounds().contains(x, y)) {
            attemptLogin();
        } else if (bRegister.getBounds().contains(x, y)) {
            GameStates.setGameState(REGISTER);
        } else if (bBack.getBounds().contains(x, y)) {
            GameStates.setGameState(STORY);
            reset();
        }
        getGame().repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isProcessing) return;
        
        if (SessionManager.getInstance().isLoggedIn()) {
            GameStates.setGameState(MENU);
            return;
        }
        
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            activeField = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            attemptLogin();
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
            activeField = activeField == 1 ? 2 : 1;
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (activeField == 1 && !username.isEmpty())
                username = username.substring(0, username.length() - 1);
            else if (activeField == 2 && !password.isEmpty())
                password = password.substring(0, password.length() - 1);
        } else {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                if (activeField == 1 && username.length() < 20)
                    username += c;
                else if (activeField == 2 && password.length() < 30)
                    password += c;
            }
        }
        getGame().repaint();
    }

    private void attemptLogin() {
        if (isProcessing) return;
        
        if (SessionManager.getInstance().isLoggedIn()) {
            message = "Already logged in!";
            messageColor = Color.GREEN;
            getGame().repaint();
            
            isProcessing = true;
            GameStates.setGameState(MENU);
            return;
        }
        
        if (username.isEmpty() || password.isEmpty()) {
            message = "Please fill all fields!";
            messageColor = Color.RED;
            getGame().repaint();
            return;
        }

        Map<String, Object> user = DatabaseManager.getInstance().loginUser(username, password);
        if (user != null) {
            // Login successful
            SessionManager.getInstance().login(
                    (int) user.get("id"),
                    (String) user.get("username"),
                    (String) user.get("email"),
                    (boolean) user.get("is_admin"));

            // Show success message
            message = "Welcome back, " + username + "!";
            messageColor = Color.GREEN;
            password = "";
            getGame().repaint();

            isProcessing = true;
            
            // FIX: Show welcome alert box using session data
            final boolean isAdminUser = (boolean) user.get("is_admin");
            
            // Show alert in a separate thread to not block UI
            new Thread(() -> {
                String welcomeMessage = isAdminUser 
                    ? "🎮 Welcome back, Admin " + SessionManager.getInstance().getUsername() + "!\n\n"
                      : "🎮 Welcome back, " + SessionManager.getInstance().getUsername() + "!\n\n" ;
                
                JOptionPane.showMessageDialog(
                    game, 
                    welcomeMessage,
                    "Login Successful", 
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                // Navigate based on user type
                if (isAdminUser) {
                    GameStates.setGameState(ADMIN_PANEL);
                } else {
                    GameStates.setGameState(LEVEL_SELECT);
                    if (GameStates.gameState == LEVEL_SELECT) {
                        game.getLevelSelect().refreshLevels();
                    }
                }
                
                game.repaint();
                isProcessing = false;
            }).start();

        } else {
            // Login failed
            message = "Invalid username or password!";
            messageColor = Color.RED;
            password = "";
            getGame().repaint();
        }
    }

    private void reset() {
        username = "";
        password = "";
        message = "";
        activeField = 0;
        isProcessing = false;
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (isProcessing) return;
        
        if (SessionManager.getInstance().isLoggedIn()) {
            GameStates.setGameState(MENU);
            return;
        }
        
        bLogin.setMouseOver(bLogin.getBounds().contains(x, y));
        bRegister.setMouseOver(bRegister.getBounds().contains(x, y));
        bBack.setMouseOver(bBack.getBounds().contains(x, y));
        getGame().repaint();
    }

    @Override
    public void mousePressed(int x, int y) {
        if (isProcessing) return;
        
        if (SessionManager.getInstance().isLoggedIn()) {
            GameStates.setGameState(MENU);
            return;
        }
        
        bLogin.setMousePressed(bLogin.getBounds().contains(x, y));
        bRegister.setMousePressed(bRegister.getBounds().contains(x, y));
        bBack.setMousePressed(bBack.getBounds().contains(x, y));
        getGame().repaint();
    }

    @Override
    public void mouseReleased(int x, int y) {
        bLogin.resetBooleans();
        bRegister.resetBooleans();
        bBack.resetBooleans();
        getGame().repaint();
    }

    @Override
    public void mouseDragged(int x, int y) {
    }
}