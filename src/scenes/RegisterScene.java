package scenes;

import static main.GameStates.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import main.Game;
import managers.DatabaseManager;
import ui.MyButton;

public class RegisterScene extends GameScene implements SceneMethods {

    private MyButton bRegister, bLogin, bBack;
    private String username = "", password = "", email = "";
    private String message = "";
    private Color messageColor = Color.RED;
    private int activeField = 0; // 1=username, 2=password, 3=email

    public RegisterScene(Game game) {
        super(game);
        initButtons();
    }

    private void initButtons() {
        int w = 200, h = 50, x = 220;
        bRegister = new MyButton("REGISTER", x, 380, w, h);
        bLogin = new MyButton("Back to Login", x, 450, w, h);
        bBack = new MyButton("Back", x, 520, w, h);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(70, 130, 180));
        g.fillRect(0, 0, 640, 800);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("REGISTER", 230, 100);

        drawField(g, "Username:", username, 150, 160, activeField == 1);
        drawField(g, "Password:", "*".repeat(password.length()), 150, 230, activeField == 2);
        drawField(g, "Email:", email, 150, 300, activeField == 3);

        if (!message.isEmpty()) {
            g.setColor(messageColor);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(message, 150, 430);
        }

        bRegister.draw(g);
        bLogin.draw(g);
        bBack.draw(g);
    }

    private void drawField(Graphics g, String label, String value, int x, int y, boolean active) {
        g.setColor(active ? Color.WHITE : Color.LIGHT_GRAY);
        g.fillRect(x, y, 340, 40);
        g.setColor(active ? Color.YELLOW : Color.CYAN);
        g.drawRect(x, y, 340, 40);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(label, x, y - 8);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        String display = activeField == 2 ? "*".repeat(password.length()) : value;
        g.drawString(display + (active ? "_" : ""), x + 10, y + 28);
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (y >= 160 && y <= 200)
            activeField = 1;
        else if (y >= 230 && y <= 270)
            activeField = 2;
        else if (y >= 300 && y <= 340)
            activeField = 3;
        else
            activeField = 0;

        if (bRegister.getBounds().contains(x, y)) {
            attemptRegister();
        } else if (bLogin.getBounds().contains(x, y)) {
            setGameState(LOGIN);
        } else if (bBack.getBounds().contains(x, y)) {
            setGameState(STORY);
            reset();
        }
        getGame().repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            activeField = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            attemptRegister();
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
            activeField = activeField == 1 ? 2 : activeField == 2 ? 3 : 1;
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (activeField == 1 && !username.isEmpty())
                username = username.substring(0, username.length() - 1);
            else if (activeField == 2 && !password.isEmpty())
                password = password.substring(0, password.length() - 1);
            else if (activeField == 3 && !email.isEmpty())
                email = email.substring(0, email.length() - 1);
        } else {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '@' || c == '.' || c == ' ') {
                if (activeField == 1 && username.length() < 20)
                    username += c;
                else if (activeField == 2 && password.length() < 30)
                    password += c;
                else if (activeField == 3 && email.length() < 50)
                    email += c;
            }
        }
        getGame().repaint();
    }

    private void attemptRegister() {
        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            message = "Please fill all fields!";
            messageColor = Color.RED;
            getGame().repaint();
            return;
        }
        if (username.length() < 3) {
            message = "Username too short (3+ chars)";
            messageColor = Color.RED;
            getGame().repaint();
            return;
        }
        if (password.length() < 6) {
            message = "Password too weak (6+ chars)";
            messageColor = Color.RED;
            getGame().repaint();
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            message = "Invalid email address";
            messageColor = Color.RED;
            getGame().repaint();
            return;
        }

        // FIXED: No alert box, just show message and navigate
        boolean success = DatabaseManager.getInstance().registerUser(username, password, email, false);
        if (success) {
            message = "Account created! Redirecting...";
            messageColor = Color.GREEN;
            getGame().repaint();

            // Navigate after brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 1 second to see success message
                } catch (InterruptedException ex) {
                    // Ignore
                }
                setGameState(LOGIN);
                reset();
            }).start();
        } else {
            message = "Username or email already taken!";
            messageColor = Color.RED;
            getGame().repaint();
        }
    }

    private void reset() {
        username = "";
        password = "";
        email = "";
        message = "";
        activeField = 0;
    }

    @Override
    public void mouseMoved(int x, int y) {
        bRegister.setMouseOver(bRegister.getBounds().contains(x, y));
        bLogin.setMouseOver(bLogin.getBounds().contains(x, y));
        bBack.setMouseOver(bBack.getBounds().contains(x, y));
        getGame().repaint();
    }

    @Override
    public void mousePressed(int x, int y) {
        bRegister.setMousePressed(bRegister.getBounds().contains(x, y));
        bLogin.setMousePressed(bLogin.getBounds().contains(x, y));
        bBack.setMousePressed(bBack.getBounds().contains(x, y));
        getGame().repaint();
    }

    @Override
    public void mouseReleased(int x, int y) {
        bRegister.resetBooleans();
        bLogin.resetBooleans();
        bBack.resetBooleans();
        getGame().repaint();
    }

    @Override
    public void mouseDragged(int x, int y) {
    }
}