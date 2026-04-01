package scenes;

import static main.GameStates.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import main.Game;
import managers.SessionManager;
import ui.MyButton;

public class Story extends GameScene implements SceneMethods {

    private MyButton bStart, bBack, bYes, bNo, bOkay, bContinue;
    private int animTick = 0;
    private BufferedImage kingImg;
    
    // Dialogue system
    private int dialogueStep = 0;
    private String[] dialogues = {
        "Hey! The enemies are attacking!",
        "Can you help us?",
        "Then help us defend the castle with your strategy!",
        "Place the towers at grass tiles at strategic locations and make sure you endure the waves of enemies!",
        "Good luck, soldier!"
    };
    
    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color GOLDEN_YELLOW = new Color(255, 215, 0);

    public Story(Game game) {
        super(game);
        loadKingImage();
        initButtons();
    }
    
    private void loadKingImage() {
        try {
            File kingFile = new File("res/king1.png");
            if (kingFile.exists()) {
                kingImg = ImageIO.read(kingFile);
                System.out.println("Successfully loaded kin1.png");
            } else {
                System.out.println("Warning: Could not find kin1.png at " + kingFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading king image");
        }
    }

    private void initButtons() {
        int w = 180;
        int h = 50;
        int x = 640 / 2 - w / 2;
        int y = 600;

        bStart = new MyButton("Continue", x, y, w, h);
        bBack = new MyButton("Back", x, y + 70, w, h);
        
        // Yes/No buttons - properly centered
        int smallW = 100;
        int smallH = 45;
        int centerX = 640 / 2;
        int smallY = 500;
        int gap = 20; // Space between buttons
        
        // Yes button: left of center
        bYes = new MyButton("Yes", centerX - smallW - gap/2, smallY, smallW, smallH);
        // No button: right of center
        bNo = new MyButton("No", centerX + gap/2, smallY, smallW, smallH);
        
        // Okay button - centered
        bOkay = new MyButton("Okay", centerX - smallW/2, smallY, smallW, smallH);
        
        // Continue button for first dialogue
        bContinue = new MyButton("Continue", centerX - w/2, smallY, w, smallH);
    }

    public void update() {
        updateTick();
        animTick++;
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background
        float pulse = (float) Math.sin(animTick * 0.05) * 0.1f + 0.9f;
        Color darkBlue = new Color(30, 30, 70);
        Color darkerBlue = new Color(20, 20, 50);
        
        GradientPaint bgGradient = new GradientPaint(
            0, 0, darkBlue,
            0, 800, darkerBlue
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, 640, 800);

        // Title
        g2d.setColor(GOLDEN_YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 42));
        String title = "STORY";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        
        // Glow effect
        g2d.setColor(new Color(255, 215, 0, 100));
        for (int i = 0; i < 5; i++) {
            g2d.drawString(title, 320 - titleWidth / 2 + i, 81 + i);
        }
        
        g2d.setColor(GOLDEN_YELLOW);
        g2d.drawString(title, 320 - titleWidth / 2, 80);

        // Draw king and dialogue
        drawKingWithDialogue(g2d);

        // Draw appropriate buttons based on dialogue step
        if (dialogueStep == 0) {
            // First dialogue - Continue button
            bContinue.draw(g);
        } else if (dialogueStep == 1) {
            // Second dialogue - Yes/No buttons
            bYes.draw(g);
            bNo.draw(g);
        } else if (dialogueStep == 2 || dialogueStep == 3) {
            // Third/Fourth dialogue - Okay button
            bOkay.draw(g);
        } else if (dialogueStep >= 4) {
            // Final buttons - Start and Back
            bStart.draw(g);
            bBack.draw(g);
        }
        
        drawDecorations(g2d);
    }
    
    private void drawKingWithDialogue(Graphics2D g2d) {
        // Draw king
        if (kingImg != null) {
            int kingWidth = 150;
            int kingHeight = (int) (kingImg.getHeight() * ((float) kingWidth / kingImg.getWidth()));
            int kingX = 640 / 2 - kingWidth / 2;
            int kingY = 150;
            
            g2d.drawImage(kingImg, kingX, kingY, kingWidth, kingHeight, null);
        }
        
        // Dialogue box
        int boxX = 100;
        int boxY = 360;
        int boxWidth = 440;
        int boxHeight = 120;
        
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(boxX + 5, boxY + 5, boxWidth, boxHeight, 20, 20);
        
        // Background
        g2d.setColor(new Color(240, 240, 240, 250));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Border
        g2d.setColor(DARK_BLUE);
        g2d.setStroke(new java.awt.BasicStroke(4));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Speech pointer
        int[] triX = {320 - 15, 320, 320 + 15};
        int[] triY = {boxY, boxY - 20, boxY};
        g2d.setColor(new Color(240, 240, 240, 250));
        g2d.fillPolygon(triX, triY, 3);
        g2d.setColor(DARK_BLUE);
        g2d.drawPolygon(triX, triY, 3);
        
        // Character name
        g2d.setColor(DARK_BLUE);
        g2d.fillRoundRect(boxX + 10, boxY - 15, 100, 30, 10, 10);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("King", boxX + 40, boxY + 7);
        
        // Dialogue text with word wrap
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(new Color(40, 40, 40));
        
        String dialogue = dialogues[Math.min(dialogueStep, dialogues.length - 1)];
        drawWrappedText(g2d, dialogue, boxX + 20, boxY + 40, boxWidth - 40, 22);
    }
    
    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int maxWidth, int lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentY = y;
        
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            int testWidth = g2d.getFontMetrics().stringWidth(testLine);
            
            if (testWidth > maxWidth && line.length() > 0) {
                g2d.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(testLine);
            }
        }
        
        if (line.length() > 0) {
            g2d.drawString(line.toString(), x, currentY);
        }
    }
    
    private void drawDecorations(Graphics2D g2d) {
        float pulse = (float) Math.sin(animTick * 0.1) * 0.5f + 0.5f;
        int alpha = (int)(pulse * 100);
        
        g2d.setColor(new Color(255, 215, 0, alpha));
        
        int size = 40;
        int thickness = 3;
        
        // Top-left
        g2d.fillRect(30, 30, size, thickness);
        g2d.fillRect(30, 30, thickness, size);
        
        // Top-right
        g2d.fillRect(640 - 30 - size, 30, size, thickness);
        g2d.fillRect(640 - 30 - thickness, 30, thickness, size);
        
        // Bottom-left
        g2d.fillRect(30, 800 - 30 - thickness, size, thickness);
        g2d.fillRect(30, 800 - 30 - size, thickness, size);
        
        // Bottom-right
        g2d.fillRect(640 - 30 - size, 800 - 30 - thickness, size, thickness);
        g2d.fillRect(640 - 30 - thickness, 800 - 30 - size, thickness, size);
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (dialogueStep == 0) {
            // First dialogue - Continue button
            if (bContinue.getBounds().contains(x, y)) {
                dialogueStep = 1;
            }
        } else if (dialogueStep == 1) {
            // Second dialogue - Yes/No
            if (bYes.getBounds().contains(x, y)) {
                dialogueStep = 2;
            } else if (bNo.getBounds().contains(x, y)) {
                // No button - go back to menu
                dialogueStep = 0; // Reset dialogue
                setGameState(MENU);
            }
        } else if (dialogueStep == 2) {
            // Third dialogue - Okay
            if (bOkay.getBounds().contains(x, y)) {
                dialogueStep = 3;
            }
        } else if (dialogueStep == 3) {
            // Fourth dialogue - Okay
            if (bOkay.getBounds().contains(x, y)) {
                dialogueStep = 4;
            }
        } else if (dialogueStep >= 4) {
            // Final buttons
            if (bStart.getBounds().contains(x, y)) {
                if (!SessionManager.getInstance().isLoggedIn()) {
                    setGameState(LOGIN);
                    return;
                }
                
                Playing playing = game.getPlaying();
                if (playing.isGameInProgress()) {
                    int choice = JOptionPane.showConfirmDialog(
                        game,
                        "You have a game in progress. Continue where you left off?",
                        "Game in Progress",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        playing.continueCurrentLevel();
                        setGameState(PLAYING);
                    } else {
                        playing.setGameInProgress(false);
                        setGameState(LEVEL_SELECT);
                    }
                } else {
                    setGameState(LEVEL_SELECT);
                }
            } else if (bBack.getBounds().contains(x, y)) {
                dialogueStep = 0; // Reset dialogue
                setGameState(MENU);
            }
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dialogueStep = 0;
            setGameState(MENU);
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (dialogueStep < 4) {
                if (dialogueStep == 1) {
                    dialogueStep = 2; // Auto-yes
                } else {
                    dialogueStep++;
                }
            } else {
                mouseClicked(320, 600);
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        // Reset all buttons
        bStart.setMouseOver(false);
        bBack.setMouseOver(false);
        bYes.setMouseOver(false);
        bNo.setMouseOver(false);
        bOkay.setMouseOver(false);
        bContinue.setMouseOver(false);

        // Set mouse over for appropriate buttons based on dialogue step
        if (dialogueStep == 0) {
            if (bContinue.getBounds().contains(x, y))
                bContinue.setMouseOver(true);
        } else if (dialogueStep == 1) {
            if (bYes.getBounds().contains(x, y))
                bYes.setMouseOver(true);
            else if (bNo.getBounds().contains(x, y))
                bNo.setMouseOver(true);
        } else if (dialogueStep == 2 || dialogueStep == 3) {
            if (bOkay.getBounds().contains(x, y))
                bOkay.setMouseOver(true);
        } else if (dialogueStep >= 4) {
            if (bStart.getBounds().contains(x, y))
                bStart.setMouseOver(true);
            else if (bBack.getBounds().contains(x, y))
                bBack.setMouseOver(true);
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (dialogueStep == 0) {
            if (bContinue.getBounds().contains(x, y))
                bContinue.setMousePressed(true);
        } else if (dialogueStep == 1) {
            if (bYes.getBounds().contains(x, y))
                bYes.setMousePressed(true);
            else if (bNo.getBounds().contains(x, y))
                bNo.setMousePressed(true);
        } else if (dialogueStep == 2 || dialogueStep == 3) {
            if (bOkay.getBounds().contains(x, y))
                bOkay.setMousePressed(true);
        } else if (dialogueStep >= 4) {
            if (bStart.getBounds().contains(x, y))
                bStart.setMousePressed(true);
            else if (bBack.getBounds().contains(x, y))
                bBack.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        bStart.resetBooleans();
        bBack.resetBooleans();
        bYes.resetBooleans();
        bNo.resetBooleans();
        bOkay.resetBooleans();
        bContinue.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {
    }
}