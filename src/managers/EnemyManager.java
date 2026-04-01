package managers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import enemies.Bat;
import enemies.Enemy;
import enemies.Knight;
import enemies.Orc;
import enemies.Wolf;
import helpz.LoadSave;
import objects.PathPoint;
import scenes.Playing;
import static helpz.Constants.Direction.*;
import static helpz.Constants.Tiles.*;
import static helpz.Constants.Enemies.*;

/**
 * Enhanced Enemy Manager with configurable health and speed multipliers
 */
public class EnemyManager {

	private Playing playing;
	private BufferedImage[] enemyImgs;
	private ArrayList<Enemy> enemies = new ArrayList<>();
	private PathPoint start, end;
	private int HPbarWidth = 20;
	private BufferedImage slowEffect;
	
	// NEW: Multipliers from level configuration
	private double healthMultiplier = 1.0;
	private double speedMultiplier = 1.0;

	public EnemyManager(Playing playing, PathPoint start, PathPoint end) {
		this.playing = playing;
		enemyImgs = new BufferedImage[4];
		this.start = start;
		this.end = end;

		loadEffectImg();
		loadEnemyImgs();
	}
	
	/**
	 * Set multipliers from level configuration
	 */
	public void setMultipliers(double healthMultiplier, double speedMultiplier) {
		this.healthMultiplier = healthMultiplier;
		this.speedMultiplier = speedMultiplier;
	}

	private void loadEffectImg() {
		slowEffect = LoadSave.getSpriteAtlas().getSubimage(32 * 9, 32 * 2, 32, 32);
	}

	private void loadEnemyImgs() {
		BufferedImage atlas = LoadSave.getSpriteAtlas();
		for (int i = 0; i < 4; i++)
			enemyImgs[i] = atlas.getSubimage(i * 32, 32, 32, 32);
	}

	public void update() {
		for (Enemy e : enemies)
			if (e.isAlive())
				updateEnemyMove(e);
	}

	public void updateEnemyMove(Enemy e) {
		if (e.getLastDir() == -1)
			setNewDirectionAndMove(e);

		// Apply speed multiplier
		float effectiveSpeed = (float)(GetSpeed(e.getEnemyType()) * speedMultiplier);
		
		int newX = (int) (e.getX() + getSpeedAndWidth(e.getLastDir(), effectiveSpeed));
		int newY = (int) (e.getY() + getSpeedAndHeight(e.getLastDir(), effectiveSpeed));

		if (getTileType(newX, newY) == ROAD_TILE) {
			e.move(effectiveSpeed, e.getLastDir());
		} else if (isAtEnd(e)) {
			e.kill();
			playing.removeOneLife();
		} else {
			setNewDirectionAndMove(e);
		}
	}

	private void setNewDirectionAndMove(Enemy e) {
		int dir = e.getLastDir();
		int xCord = (int) (e.getX() / 32);
		int yCord = (int) (e.getY() / 32);

		fixEnemyOffsetTile(e, dir, xCord, yCord);

		if (isAtEnd(e))
			return;

		float effectiveSpeed = (float)(GetSpeed(e.getEnemyType()) * speedMultiplier);

		if (dir == LEFT || dir == RIGHT) {
			int newY = (int) (e.getY() + getSpeedAndHeight(UP, effectiveSpeed));
			if (getTileType((int) e.getX(), newY) == ROAD_TILE)
				e.move(effectiveSpeed, UP);
			else
				e.move(effectiveSpeed, DOWN);
		} else {
			int newX = (int) (e.getX() + getSpeedAndWidth(RIGHT, effectiveSpeed));
			if (getTileType(newX, (int) e.getY()) == ROAD_TILE)
				e.move(effectiveSpeed, RIGHT);
			else
				e.move(effectiveSpeed, LEFT);
		}
	}

	private void fixEnemyOffsetTile(Enemy e, int dir, int xCord, int yCord) {
		switch (dir) {
		case RIGHT:
			if (xCord < 19)
				xCord++;
			break;
		case DOWN:
			if (yCord < 19)
				yCord++;
			break;
		}

		e.setPos(xCord * 32, yCord * 32);
	}

	private boolean isAtEnd(Enemy e) {
		if (e.getX() == end.getxCord() * 32)
			if (e.getY() == end.getyCord() * 32)
				return true;
		return false;
	}

	private int getTileType(int x, int y) {
		return playing.getTileType(x, y);
	}

	private float getSpeedAndHeight(int dir, float speed) {
		if (dir == UP)
			return -speed;
		else if (dir == DOWN)
			return speed + 32;
		return 0;
	}

	private float getSpeedAndWidth(int dir, float speed) {
		if (dir == LEFT)
			return -speed;
		else if (dir == RIGHT)
			return speed + 32;
		return 0;
	}
	
	// Helper method for base speed
	private float GetSpeed(int enemyType) {
		return helpz.Constants.Enemies.GetSpeed(enemyType);
	}

	public void spawnEnemy(int nextEnemy) {
		addEnemy(nextEnemy);
	}

	public void addEnemy(int enemyType) {
		int x = start.getxCord() * 32;
		int y = start.getyCord() * 32;

		Enemy newEnemy = null;
		
		switch (enemyType) {
		case ORC:
			newEnemy = new Orc(x, y, 0, this);
			break;
		case BAT:
			newEnemy = new Bat(x, y, 0, this);
			break;
		case KNIGHT:
			newEnemy = new Knight(x, y, 0, this);
			break;
		case WOLF:
			newEnemy = new Wolf(x, y, 0, this);
			break;
		}
		
		// Apply health multiplier to the new enemy
		if (newEnemy != null) {
			int baseHealth = newEnemy.getMaxHealth();
			int scaledHealth = (int)(baseHealth * healthMultiplier);
			newEnemy.setMaxHealth(scaledHealth);
			newEnemy.heal(scaledHealth); // Set current health to max
			enemies.add(newEnemy);
		}
	}

	public void draw(Graphics g) {
		for (Enemy e : enemies) {
			if (e.isAlive()) {
				drawEnemy(e, g);
				drawHealthBar(e, g);
				drawEffects(e, g);
			}
		}
	}

	private void drawEffects(Enemy e, Graphics g) {
		if (e.isSlowed())
			g.drawImage(slowEffect, (int) e.getX(), (int) e.getY(), null);
	}

	private void drawHealthBar(Enemy e, Graphics g) {
		g.setColor(Color.red);
		g.fillRect((int) e.getX() + 16 - (getNewBarWidth(e) / 2), (int) e.getY() - 10, getNewBarWidth(e), 3);
	}

	private int getNewBarWidth(Enemy e) {
		return (int) (HPbarWidth * e.getHealthBarFloat());
	}

	private void drawEnemy(Enemy e, Graphics g) {
		g.drawImage(enemyImgs[e.getEnemyType()], (int) e.getX(), (int) e.getY(), null);
	}

	public ArrayList<Enemy> getEnemies() {
		return enemies;
	}

	public int getAmountOfAliveEnemies() {
		int size = 0;
		for (Enemy e : enemies)
			if (e.isAlive())
				size++;
		return size;
	}

	public void rewardPlayer(int enemyType) {
		playing.rewardPlayer(enemyType);
	}

	public void reset() {
		enemies.clear();
	}

	public PathPoint getStartPoint() {
		return start;
	}
	
	public PathPoint getEndPoint() {
		return end;
	}
}