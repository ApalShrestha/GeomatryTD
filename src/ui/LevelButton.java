package ui;

import java.awt.image.BufferedImage;

public class LevelButton extends MyButton {
    private int levelId;
    private String levelName;
    private BufferedImage preview;

    public LevelButton(int x, int y, int width, int height, int levelId, String levelName, BufferedImage preview) {
        super("", x, y, width, height);
        this.levelId = levelId;
        this.levelName = levelName;
        this.preview = preview;
    }

    public int getLevelId() { return levelId; }
    public String getLevelName() { return levelName; }
    public BufferedImage getPreview() { return preview; }
}
