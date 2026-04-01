package managers;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.PriorityQueue;

import enemies.Enemy;
import helpz.LoadSave;
import helpz.Constants.Enemies;
import objects.Tower;
import scenes.Playing;
import objects.PathPoint;

public class TowerManager {

    private Playing playing;
    private BufferedImage[] towerImgs;
    private ArrayList<Tower> towers = new ArrayList<>();
    private int towerAmount = 0;

    public TowerManager(Playing playing) {
        this.playing = playing;
        loadTowerImgs();
    }

    private void loadTowerImgs() {
        BufferedImage atlas = LoadSave.getSpriteAtlas();
        towerImgs = new BufferedImage[3];
        for (int i = 0; i < 3; i++)
            towerImgs[i] = atlas.getSubimage((4 + i) * 32, 32, 32, 32);
    }

    public void addTower(Tower selectedTower, int xPos, int yPos) {
        towers.add(new Tower(xPos, yPos, towerAmount++, selectedTower.getTowerType()));
    }

    public void removeTower(Tower displayedTower) {
        for (int i = 0; i < towers.size(); i++)
            if (towers.get(i).getId() == displayedTower.getId())
                towers.remove(i);
    }

    public void upgradeTower(Tower displayedTower) {
        for (Tower t : towers)
            if (t.getId() == displayedTower.getId())
                t.upgradeTower();
    }

    public void update() {
        for (Tower t : towers) {
            t.update();
            attackEnemyWithPriority(t);
        }
    }

    private void attackEnemyWithPriority(Tower t) {
        Enemy target = findPriorityTarget(t);
        if (target != null && target.isAlive()) {
            if (isEnemyInRange(t, target)) {
                if (t.isCooldownOver()) {
                    playing.shootEnemy(t, target);
                    t.resetCooldown();
                }
            }
        }
    }

    private Enemy findPriorityTarget(Tower t) {
        PriorityQueue<Enemy> enemyQueue = new PriorityQueue<>((e1, e2) -> {
            // Priority 1: Progress (distance to end) - Greedy Algorithm
            float progress1 = calculateProgress(e1);
            float progress2 = calculateProgress(e2);
            
            if (Math.abs(progress1 - progress2) > 0.05f) { // 5% threshold
                return Float.compare(progress2, progress1); // Higher progress first
            }
            
            // Priority 2: Threat Level (based on enemy attributes)
            double threat1 = calculateThreatLevel(e1);
            double threat2 = calculateThreatLevel(e2);
            
            if (Math.abs(threat1 - threat2) > 0.1) {
                return Double.compare(threat2, threat1); // Higher threat first
            }
            
            // Priority 3: Low health enemies (finish them off)
            float healthPercent1 = e1.getHealth() / (float) Enemies.GetStartHealth(e1.getEnemyType());
            float healthPercent2 = e2.getHealth() / (float) Enemies.GetStartHealth(e2.getEnemyType());
            
            if (Math.abs(healthPercent1 - healthPercent2) > 0.1f) {
                return Float.compare(healthPercent1, healthPercent2); // Lower health % first
            }
            
            // Priority 4: Enemy type priority
            int typePriority1 = getEnemyTypePriority(e1.getEnemyType());
            int typePriority2 = getEnemyTypePriority(e2.getEnemyType());
            
            return Integer.compare(typePriority2, typePriority1); // Higher priority first
        });

        // Add enemies in range to priority queue
        for (Enemy e : playing.getEnemyManger().getEnemies()) {
            if (e.isAlive() && isEnemyInRange(t, e)) {
                enemyQueue.offer(e);
            }
        }

        return enemyQueue.poll(); // Get the highest priority enemy
    }

    /**
     * Calculate threat level based on enemy attributes:
     * - Health: Higher health = more threatening
     * - Speed: Faster enemies reach the end quicker
     * - Reward value: Higher reward = more dangerous enemy
     * 
     * Enemy Statistics:
     * ORC:    Health=85,  Speed=0.5,  Reward=5   (Tank - slow but moderate health)
     * BAT:    Health=100, Speed=0.7,  Reward=5   (Fast flyer - moderate threat)
     * KNIGHT: Health=400, Speed=0.45, Reward=25  (Boss - highest threat, very tanky)
     * WOLF:   Health=125, Speed=0.85, Reward=10  (Speed demon - fastest enemy)
     */
    private double calculateThreatLevel(Enemy e) {
        int type = e.getEnemyType();
        int currentHealth = e.getHealth();
        float speed = Enemies.GetSpeed(type);
        int reward = Enemies.GetReward(type);
        int maxHealth = Enemies.GetStartHealth(type);
        
        // Normalize values to 0-1 scale
        double healthFactor = currentHealth / 400.0; // KNIGHT has max 400 health
        double speedFactor = speed / 0.85; // WOLF has max 0.85 speed
        double rewardFactor = reward / 25.0; // KNIGHT has max 25 reward
        
        // Weighted threat calculation
        // Speed is most important (40%) - fast enemies reach end quickly
        // Health is secondary (35%) - tanky enemies survive longer
        // Reward is indicator of overall danger (25%)
        double threat = (speedFactor * 0.40) + (healthFactor * 0.35) + (rewardFactor * 0.25);
        
        return threat;
    }

    /**
     * Get static priority for enemy types
     * Higher value = higher priority to target
     * 
     * Priority reasoning:
     * 1. KNIGHT (4) - Boss enemy, highest health & reward, must focus
     * 2. WOLF (3) - Fastest enemy, will reach end quickly
     * 3. BAT (2) - Moderate speed and health, standard threat
     * 4. ORC (1) - Slowest, easiest to deal with later
     */
    private int getEnemyTypePriority(int enemyType) {
        switch (enemyType) {
            case Enemies.KNIGHT:
                return 4; // Highest priority - boss enemy
            case Enemies.WOLF:
                return 3; // High priority - speed threat
            case Enemies.BAT:
                return 2; // Medium priority - balanced threat
            case Enemies.ORC:
                return 1; // Low priority - slow tank
            default:
                return 0;
        }
    }

    private float calculateProgress(Enemy e) {
        // Calculate how close enemy is to the end point (greedy approach)
        PathPoint start = playing.getEnemyManger().getStartPoint();
        PathPoint end = playing.getEnemyManger().getEndPoint();
        
        if (start == null || end == null) return 0f;
        
        // Current distance to end
        float currentDist = helpz.Utilz.GetHypoDistance(e.getX(), e.getY(), 
                                                        end.getxCord() * 32, end.getyCord() * 32);
        
        // Total distance from start to end
        float totalDist = helpz.Utilz.GetHypoDistance(
            start.getxCord() * 32, 
            start.getyCord() * 32, 
            end.getxCord() * 32, 
            end.getyCord() * 32
        );
        
        // Progress is how much distance has been covered (1.0 = at end, 0.0 = at start)
        return 1.0f - (currentDist / totalDist);
    }

    private boolean isEnemyInRange(Tower t, Enemy e) {
        int range = helpz.Utilz.GetHypoDistance(t.getX(), t.getY(), e.getX(), e.getY());
        return range < t.getRange();
    }

    public void draw(Graphics g) {
        for (Tower t : towers)
            g.drawImage(towerImgs[t.getTowerType()], t.getX(), t.getY(), null);
    }

    public Tower getTowerAt(int x, int y) {
        for (Tower t : towers)
            if (t.getX() == x)
                if (t.getY() == y)
                    return t;
        return null;
    }

    public BufferedImage[] getTowerImgs() {
        return towerImgs;
    }

    public void reset() {
        towers.clear();
        towerAmount = 0;
    }
}