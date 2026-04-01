package managers;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all database operations using MySQL (XAMPP)
 * Handles users, levels, progress, and scores
 */
public class DatabaseManager {

    // MySQL Connection Settings
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "geomatry_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; // Default XAMPP password is empty

    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private Connection connection;

    // Singleton instance
    private static DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialize database and create tables
     */
    private void initDatabase() {
        System.out.println("🔧 Initializing Database...");

        try {
            // Load MySQL JDBC driver
            System.out.println("📦 Loading MySQL JDBC driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully!");

            // First, connect without specifying database to create it
            String createDbUrl = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

            System.out.println("🔗 Connecting to MySQL server: " + createDbUrl);
            Connection tempConn = DriverManager.getConnection(createDbUrl, DB_USER, DB_PASS);
            System.out.println("✅ Connected to MySQL server!");

            // Create database if not exists
            Statement stmt = tempConn.createStatement();
            System.out.println("🗃️ Creating database: " + DB_NAME);
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("✅ Database '" + DB_NAME + "' ready!");
            stmt.close();
            tempConn.close();

            // Now connect to the database
            System.out.println("🔗 Connecting to database: " + DB_URL);
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ Connected to database '" + DB_NAME + "'!");

            createTables();
            createAdminAccount();
            cleanupDuplicateHighScores();

            System.out.println("🎉 Database initialization completed successfully!");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            System.err.println("💡 Download from: https://dev.mysql.com/downloads/connector/j/");
            System.err.println("💡 Place the JAR file in your lib folder and add to classpath");
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed!");
            System.err.println("💡 Make sure:");
            System.err.println("   - XAMPP MySQL is running");
            System.err.println("   - MySQL service is started in XAMPP");
            System.err.println("   - No firewall is blocking port 3306");
            System.err.println("💡 Error details: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create all necessary tables
     */
    private void createTables() throws SQLException {
        System.out.println("🗂️ Creating tables...");
        Statement stmt = connection.createStatement();

        // Users table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "username VARCHAR(50) UNIQUE NOT NULL," +
                        "password VARCHAR(255) NOT NULL," +
                        "email VARCHAR(100)," +
                        "is_admin TINYINT DEFAULT 0," +
                        "is_active TINYINT DEFAULT 1," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "last_login TIMESTAMP NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // Levels table (admin-created levels) - UPDATED WITH enemy_config
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS levels (" +
                        "level_id INT PRIMARY KEY," +
                        "level_name VARCHAR(100) NOT NULL," +
                        "map_data TEXT NOT NULL," +
                        "start_x INT NOT NULL," +
                        "start_y INT NOT NULL," +
                        "end_x INT NOT NULL," +
                        "end_y INT NOT NULL," +
                        "difficulty INT DEFAULT 1," +
                        "enemy_config TEXT," + // NEW: Enemy configuration
                        "is_active TINYINT DEFAULT 1," +
                        "created_by INT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // User progress table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS user_progress (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id INT NOT NULL," +
                        "level_id INT NOT NULL," +
                        "stars INT DEFAULT 0," +
                        "high_score INT DEFAULT 0," +
                        "times_completed INT DEFAULT 0," +
                        "is_unlocked TINYINT DEFAULT 0," +
                        "last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (level_id) REFERENCES levels(level_id) ON DELETE CASCADE," +
                        "UNIQUE KEY unique_user_level (user_id, level_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // Game saves table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS game_saves (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id INT NOT NULL," +
                        "level_id INT NOT NULL," +
                        "save_name VARCHAR(50) NOT NULL," +
                        "current_wave INT DEFAULT 0," +
                        "gold INT DEFAULT 100," +
                        "lives INT DEFAULT 25," +
                        "towers_data TEXT," +
                        "enemies_killed INT DEFAULT 0," +
                        "save_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // High scores table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS high_scores (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id INT NOT NULL," +
                        "level_id INT NOT NULL," +
                        "score INT NOT NULL," +
                        "stars INT NOT NULL," +
                        "enemies_killed INT NOT NULL," +
                        "achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (level_id) REFERENCES levels(level_id) ON DELETE CASCADE," +
                        "INDEX idx_score (score DESC)," +
                        "INDEX idx_user_level (user_id, level_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        stmt.close();
        System.out.println("✅ All tables created successfully!");
    }

    /**
     * Create default admin account
     */
    private void createAdminAccount() throws SQLException {
        System.out.println("Checking for admin account...");

        String checkSql = "SELECT id, is_admin FROM users WHERE username = 'admin'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(checkSql);

        if (rs.next()) {
            int adminId = rs.getInt("id");
            boolean isAdmin = rs.getInt("is_admin") == 1;

            if (isAdmin) {
                System.out.println("Admin account already exists and active (ID: " + adminId + ")");
            } else {
                String updateSql = "UPDATE users SET is_admin = 1 WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(updateSql);
                pstmt.setInt(1, adminId);
                pstmt.executeUpdate();
                pstmt.close();
                System.out.println("Fixed: User 'admin' now has admin privileges!");
            }
        } else {
            System.out.println("No 'admin' user found → Creating default admin account...");
            registerUser("admin", "admin123", "admin@geomatry.com", true);
            System.out.println("");
            System.out.println("ADMIN ACCOUNT CREATED!");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println("   → Log in now and create your levels!");
            System.out.println("");
        }

        rs.close();
        stmt.close();
    }

    /**
     * Add enemy_config column if it doesn't exist (migration helper)
     */
    public void addEnemyConfigColumn() {
        try {
            // Check if column already exists
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "levels", "enemy_config");

            if (!columns.next()) {
                // Column doesn't exist, add it
                String sql = "ALTER TABLE levels ADD COLUMN enemy_config TEXT AFTER difficulty";
                Statement stmt = connection.createStatement();
                stmt.execute(sql);
                stmt.close();
                System.out.println("✅ Added enemy_config column to levels table");

                // Update existing levels with default configurations
                migrateExistingLevels();
            } else {
                System.out.println("✅ enemy_config column already exists");
            }
            columns.close();
        } catch (SQLException e) {
            System.err.println("❌ Error adding enemy_config column: " + e.getMessage());
        }
    }

    /**
     * Migrate existing levels to have default enemy configurations
     */
    public void migrateExistingLevels() {
        try {
            System.out.println("🔄 Migrating existing levels with default enemy configurations...");

            // Get all levels without enemy_config or with NULL config
            String sql = "SELECT level_id, difficulty FROM levels WHERE enemy_config IS NULL OR enemy_config = ''";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int migratedCount = 0;
            while (rs.next()) {
                int levelId = rs.getInt("level_id");
                int difficulty = rs.getInt("difficulty");

                // Create default configuration based on difficulty
                String defaultConfig = createDefaultConfig(difficulty);

                // Update level with default config
                String updateSql = "UPDATE levels SET enemy_config = ? WHERE level_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(updateSql);
                pstmt.setString(1, defaultConfig);
                pstmt.setInt(2, levelId);
                pstmt.executeUpdate();
                pstmt.close();

                migratedCount++;
            }

            rs.close();
            stmt.close();
            System.out.println("✅ Migrated " + migratedCount + " levels with default configurations");

        } catch (SQLException e) {
            System.err.println("❌ Error migrating existing levels: " + e.getMessage());
        }
    }

    /**
     * Create default enemy configuration string based on difficulty
     */
    private String createDefaultConfig(int difficulty) {
        switch (difficulty) {
            case 1: // Easy
                return "5,4,2,1.0,1.0"; // waves, baseEnemies, increase, healthMult, speedMult
            case 2: // Medium
                return "8,6,3,1.5,1.2";
            case 3: // Hard
                return "12,8,4,2.0,1.5";
            default:
                return "5,4,2,1.0,1.0"; // Default to easy
        }
    }

    /**
     * Simple password hashing (for development - use proper hashing in production)
     */
    private String hashPassword(String password) {
        // Simple hash for development - replace with BCrypt in production
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return password; // Fallback to plaintext if hashing fails
        }
    }

    /**
     * Register a new user
     */
    public boolean registerUser(String username, String password, String email, boolean isAdmin) {
        try {
            // ←←← FIX 1: BLOCK "admin" USERNAME FOR NORMAL PLAYERS
            if (username.equalsIgnoreCase("admin") && !isAdmin) {
                System.err.println("Registration blocked: 'admin' is a reserved username!");
                return false;
            }
            if (username.toLowerCase().contains("admin") && !isAdmin) {
                System.err.println("Registration blocked: Username cannot contain 'admin'");
                return false;
            }

            // ←←← ORIGINAL CHECK (keep this)
            if (checkUsernameExists(username)) {
                System.err.println("Username already exists: " + username);
                return false;
            }

            String hashedPassword = hashPassword(password);
            String sql = "INSERT INTO users (username, password, email, is_admin) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, email);
            pstmt.setInt(4, isAdmin ? 1 : 0);
            pstmt.executeUpdate();
            pstmt.close();

            // Unlock level 1 for new user
            if (!isAdmin) {
                int userId = getUserId(username);
                if (userId != -1) {
                    unlockLevel(userId, 1);
                }
            }

            System.out.println("User registered: " + username + (isAdmin ? " (ADMIN)" : ""));
            return true;
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Login user
     */
    public Map<String, Object> loginUser(String username, String password) {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String hashedPassword = hashPassword(password);
            String sql = "SELECT id, username, email, is_admin FROM users WHERE username = ? AND password = ? AND is_active = 1";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("is_admin", rs.getInt("is_admin") == 1);

                // Update last login
                updateLastLogin(rs.getInt("id"));

                rs.close();
                pstmt.close();

                System.out.println("✅ User logged in: " + username);
                return user;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Login failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update last login time
     */
    private void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    /**
     * Get user ID by username
     */
    public int getUserId(String username) {
        try {
            String sql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                pstmt.close();
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Check if username already exists
     */
    public boolean checkUsernameExists(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                rs.close();
                pstmt.close();
                return exists;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all users (admin only)
     */
    public ArrayList<Map<String, Object>> getAllUsers() {
        ArrayList<Map<String, Object>> users = new ArrayList<>();
        try {
            String sql = "SELECT id, username, email, is_admin, is_active, created_at, last_login FROM users ORDER BY id";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("is_admin", rs.getInt("is_admin") == 1);
                user.put("is_active", rs.getInt("is_active") == 1);
                user.put("created_at", rs.getString("created_at"));
                user.put("last_login", rs.getString("last_login"));
                users.add(user);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Delete user (admin only)
     */
    public boolean deleteUser(int userId) {
        try {
            // Don't allow deleting admin
            String checkSql = "SELECT is_admin FROM users WHERE id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("is_admin") == 1) {
                System.out.println("Cannot delete admin user!");
                rs.close();
                checkStmt.close();
                return false;
            }
            rs.close();
            checkStmt.close();

            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deactivate/Activate user (admin only)
     */
    public boolean toggleUserActive(int userId) {
        try {
            String sql = "UPDATE users SET is_active = NOT is_active WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NEW: Check if level exists
     */
    private boolean levelExists(int levelId) {
        try {
            String sql = "SELECT COUNT(*) FROM levels WHERE level_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, levelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                rs.close();
                pstmt.close();
                return exists;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * NEW: Serialize map data to string
     */
    private String serializeMap(int[][] mapData) {
        StringBuilder mapStr = new StringBuilder();
        for (int y = 0; y < mapData.length; y++) {
            for (int x = 0; x < mapData[y].length; x++) {
                mapStr.append(mapData[y][x]);
                if (x < mapData[y].length - 1)
                    mapStr.append(",");
            }
            if (y < mapData.length - 1)
                mapStr.append(";");
        }
        return mapStr.toString();
    }

    /**
     * NEW: Deserialize map data from string
     */
    private int[][] deserializeMap(String mapDataStr) {
        String[] rows = mapDataStr.split(";");
        int[][] mapData = new int[rows.length][];

        for (int y = 0; y < rows.length; y++) {
            String[] cols = rows[y].split(",");
            mapData[y] = new int[cols.length];
            for (int x = 0; x < cols.length; x++) {
                mapData[y][x] = Integer.parseInt(cols[x]);
            }
        }
        return mapData;
    }

    /**
     * Save a level (original method for backward compatibility)
     */
    public boolean saveLevel(int levelId, String levelName, int[][] mapData,
            int startX, int startY, int endX, int endY, int difficulty, int createdBy) {
        try {
            // Convert map data to string
            StringBuilder mapStr = new StringBuilder();
            for (int y = 0; y < mapData.length; y++) {
                for (int x = 0; x < mapData[y].length; x++) {
                    mapStr.append(mapData[y][x]);
                    if (x < mapData[y].length - 1)
                        mapStr.append(",");
                }
                if (y < mapData.length - 1)
                    mapStr.append(";");
            }

            String sql = "INSERT INTO levels (level_id, level_name, map_data, start_x, start_y, end_x, end_y, difficulty, created_by) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE level_name = ?, map_data = ?, start_x = ?, start_y = ?, end_x = ?, end_y = ?, difficulty = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, levelId);
            pstmt.setString(2, levelName);
            pstmt.setString(3, mapStr.toString());
            pstmt.setInt(4, startX);
            pstmt.setInt(5, startY);
            pstmt.setInt(6, endX);
            pstmt.setInt(7, endY);
            pstmt.setInt(8, difficulty);
            pstmt.setInt(9, createdBy);
            // For UPDATE part
            pstmt.setString(10, levelName);
            pstmt.setString(11, mapStr.toString());
            pstmt.setInt(12, startX);
            pstmt.setInt(13, startY);
            pstmt.setInt(14, endX);
            pstmt.setInt(15, endY);
            pstmt.setInt(16, difficulty);

            pstmt.executeUpdate();
            pstmt.close();

            System.out.println("✅ Level " + levelId + " saved (without enemy config)!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Failed to save level: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save level with enemy configuration
     */
    public boolean saveLevelWithConfig(int levelId, String levelName, int[][] mapData,
            int startX, int startY, int endX, int endY,
            int difficulty, int userId, String enemyConfig) {
        try {
            String mapDataStr = serializeMap(mapData);
            String sql;

            if (levelExists(levelId)) {
                sql = "UPDATE levels SET level_name = ?, map_data = ?, start_x = ?, start_y = ?, " +
                        "end_x = ?, end_y = ?, difficulty = ?, created_by = ?, enemy_config = ? " +
                        "WHERE level_id = ?";
            } else {
                sql = "INSERT INTO levels (level_name, map_data, start_x, start_y, end_x, end_y, " +
                        "difficulty, created_by, enemy_config, level_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, levelName);
            pstmt.setString(2, mapDataStr);
            pstmt.setInt(3, startX);
            pstmt.setInt(4, startY);
            pstmt.setInt(5, endX);
            pstmt.setInt(6, endY);
            pstmt.setInt(7, difficulty);
            pstmt.setInt(8, userId);
            pstmt.setString(9, enemyConfig);
            pstmt.setInt(10, levelId);

            pstmt.executeUpdate();
            pstmt.close();

            System.out.println("✅ Level saved with enemy configuration: " + levelName);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error saving level with config: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load level with enemy configuration
     */
    public Map<String, Object> loadLevel(int levelId) {
        try {
            String sql = "SELECT * FROM levels WHERE level_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, levelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> levelData = new HashMap<>();
                levelData.put("level_id", rs.getInt("level_id"));
                levelData.put("level_name", rs.getString("level_name"));
                levelData.put("map_data", deserializeMap(rs.getString("map_data")));
                levelData.put("start_x", rs.getInt("start_x"));
                levelData.put("start_y", rs.getInt("start_y"));
                levelData.put("end_x", rs.getInt("end_x"));
                levelData.put("end_y", rs.getInt("end_y"));
                levelData.put("difficulty", rs.getInt("difficulty"));
                levelData.put("is_active", rs.getInt("is_active") == 1);
                levelData.put("created_by", rs.getInt("created_by"));
                levelData.put("enemy_config", rs.getString("enemy_config")); // Load enemy configuration

                rs.close();
                pstmt.close();
                return levelData;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Error loading level: " + e.getMessage());
        }

        return null;
    }

    /**
     * NEW: Update level configuration
     */
    public boolean updateLevelConfig(int levelId, String enemyConfig) {
        try {
            String sql = "UPDATE levels SET enemy_config = ? WHERE level_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, enemyConfig);
            pstmt.setInt(2, levelId);

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error updating level config: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all levels with configurations
     */
    public ArrayList<Map<String, Object>> getAllLevels() {
        ArrayList<Map<String, Object>> levels = new ArrayList<>();
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT level_id, level_name, difficulty, enemy_config, is_active, created_at " +
                    "FROM levels ORDER BY level_id";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> level = new HashMap<>();
                level.put("level_id", rs.getInt("level_id"));
                level.put("level_name", rs.getString("level_name"));
                level.put("difficulty", rs.getInt("difficulty"));
                level.put("enemy_config", rs.getString("enemy_config")); // Include config
                level.put("is_active", rs.getInt("is_active") == 1);
                level.put("created_at", rs.getString("created_at"));
                levels.add(level);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to get levels: " + e.getMessage());
            e.printStackTrace();
        }
        return levels;
    }

    /**
     * Delete level (admin only)
     */
    public boolean deleteLevel(int levelId) {
        try {
            String sql = "DELETE FROM levels WHERE level_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, levelId);
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unlock a level for user
     */
    public void unlockLevel(int userId, int levelId) {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "INSERT INTO user_progress (user_id, level_id, is_unlocked) " +
                    "VALUES (?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE is_unlocked = 1";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, levelId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to unlock level: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if level is unlocked
     */
    public boolean isLevelUnlocked(int userId, int levelId) {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT is_unlocked FROM user_progress WHERE user_id = ? AND level_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, levelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean unlocked = rs.getInt("is_unlocked") == 1;
                rs.close();
                pstmt.close();
                return unlocked;
            }
            // If no record exists, check if it's level 1 (always unlocked)
            rs.close();
            pstmt.close();
            return levelId == 1;

        } catch (SQLException e) {
            System.err.println("❌ Error checking level unlock: " + e.getMessage());
            e.printStackTrace();
            return levelId == 1; // Fallback
        }
    }

    /**
     * Get user's specific stats for a level
     */
    public Map<String, Object> getUserLevelStats(int userId, int levelId) {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT stars, high_score, times_completed FROM user_progress WHERE user_id = ? AND level_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, levelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("stars", rs.getInt("stars"));
                stats.put("high_score", rs.getInt("high_score"));
                stats.put("times_completed", rs.getInt("times_completed"));
                rs.close();
                pstmt.close();
                return stats;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to get user level stats: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all progress for a specific user
     */
    public ArrayList<Map<String, Object>> getUserProgress(int userId) {
        ArrayList<Map<String, Object>> progress = new ArrayList<>();
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT level_id, stars, high_score, times_completed, is_unlocked FROM user_progress WHERE user_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> levelProgress = new HashMap<>();
                levelProgress.put("level_id", rs.getInt("level_id"));
                levelProgress.put("stars", rs.getInt("stars"));
                levelProgress.put("high_score", rs.getInt("high_score"));
                levelProgress.put("times_completed", rs.getInt("times_completed"));
                levelProgress.put("is_unlocked", rs.getInt("is_unlocked") == 1);
                progress.add(levelProgress);
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to get user progress: " + e.getMessage());
            e.printStackTrace();
        }
        return progress;
    }

    /**
     * Update user progress
     */
    public void updateProgress(int userId, int levelId, int stars, int score, int enemiesKilled) {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            // First ensure the level is unlocked
            unlockLevel(userId, levelId);

            String sql = "INSERT INTO user_progress (user_id, level_id, stars, high_score, times_completed, is_unlocked) "
                    +
                    "VALUES (?, ?, ?, ?, 1, 1) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "stars = GREATEST(stars, ?), " +
                    "high_score = GREATEST(high_score, ?), " +
                    "times_completed = times_completed + 1, " +
                    "last_played = NOW()";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, levelId);
            pstmt.setInt(3, stars);
            pstmt.setInt(4, score);
            pstmt.setInt(5, stars);
            pstmt.setInt(6, score);
            pstmt.executeUpdate();
            pstmt.close();

            // Save to high scores
            saveHighScore(userId, levelId, score, stars, enemiesKilled);

            // Unlock next level
            unlockLevel(userId, levelId + 1);

            System.out.println("✅ Progress updated for user " + userId + " on level " + levelId);

        } catch (SQLException e) {
            System.err.println("❌ Error updating progress: " + e.getMessage());
            e.printStackTrace();

            // Try to reconnect and retry once
            try {
                reconnect();
                System.out.println("🔄 Retrying progress update after reconnection...");
                // You could add a retry logic here if needed
            } catch (Exception ex) {
                System.err.println("❌ Failed to recover from database error");
            }
        }
    }

    /**
     * Save high score - Only saves if it's better than existing score
     * FIX: Now checks if score already exists and only updates if new score is
     * higher
     */
    public void saveHighScore(int userId, int levelId, int score, int stars, int enemiesKilled) {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            // Check if user already has a score for this level
            String checkSql = "SELECT id, score FROM high_scores WHERE user_id = ? AND level_id = ? ORDER BY score DESC LIMIT 1";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, levelId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // User has an existing score
                int existingScore = rs.getInt("score");
                int recordId = rs.getInt("id");

                if (score > existingScore) {
                    // New score is better - UPDATE the existing record
                    String updateSql = "UPDATE high_scores SET score = ?, stars = ?, enemies_killed = ?, achieved_at = NOW() WHERE id = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                    updateStmt.setInt(1, score);
                    updateStmt.setInt(2, stars);
                    updateStmt.setInt(3, enemiesKilled);
                    updateStmt.setInt(4, recordId);
                    updateStmt.executeUpdate();
                    updateStmt.close();

                    System.out.println("✅ High score UPDATED for user " + userId + " on level " + levelId + " (New: "
                            + score + " > Old: " + existingScore + ")");
                } else {
                    // Existing score is better or equal - don't save
                    System.out.println(
                            "ℹ️ Score not saved - existing score is better (" + existingScore + " >= " + score + ")");
                }
            } else {
                // No existing score - INSERT new record
                String insertSql = "INSERT INTO high_scores (user_id, level_id, score, stars, enemies_killed) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, levelId);
                insertStmt.setInt(3, score);
                insertStmt.setInt(4, stars);
                insertStmt.setInt(5, enemiesKilled);
                insertStmt.executeUpdate();
                insertStmt.close();

                System.out.println("✅ New high score saved for user " + userId + " on level " + levelId);
            }

            rs.close();
            checkStmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Failed to save high score: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ONE-TIME CLEANUP: Remove duplicate high scores and keep only the best score
     * for each user on each level. Call this once after applying the fix.
     * 
     * Add this method to your DatabaseManager class and call it from initDatabase()
     */
    public void cleanupDuplicateHighScores() {
        try {
            System.out.println("🧹 Cleaning up duplicate high scores...");

            // Check connection
            if (!isConnectionValid()) {
                reconnect();
            }

            // Find all user-level combinations that have multiple scores
            String findDuplicatesSql = "SELECT user_id, level_id, COUNT(*) as score_count " +
                    "FROM high_scores " +
                    "GROUP BY user_id, level_id " +
                    "HAVING score_count > 1";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(findDuplicatesSql);

            int cleanedCount = 0;

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int levelId = rs.getInt("level_id");
                int scoreCount = rs.getInt("score_count");

                System.out.println("  Found " + scoreCount + " scores for User " + userId + " on Level " + levelId);

                // Find the highest score for this user-level combination
                String findBestScoreSql = "SELECT id, score FROM high_scores " +
                        "WHERE user_id = ? AND level_id = ? " +
                        "ORDER BY score DESC, achieved_at DESC LIMIT 1";

                PreparedStatement findBestStmt = connection.prepareStatement(findBestScoreSql);
                findBestStmt.setInt(1, userId);
                findBestStmt.setInt(2, levelId);
                ResultSet bestRs = findBestStmt.executeQuery();

                if (bestRs.next()) {
                    int bestScoreId = bestRs.getInt("id");
                    int bestScore = bestRs.getInt("score");

                    // Delete all OTHER scores for this user-level (keep only the best)
                    String deleteOthersSql = "DELETE FROM high_scores " +
                            "WHERE user_id = ? AND level_id = ? AND id != ?";

                    PreparedStatement deleteStmt = connection.prepareStatement(deleteOthersSql);
                    deleteStmt.setInt(1, userId);
                    deleteStmt.setInt(2, levelId);
                    deleteStmt.setInt(3, bestScoreId);

                    int deletedRows = deleteStmt.executeUpdate();

                    System.out.println(
                            "    ✓ Kept best score: " + bestScore + ", removed " + deletedRows + " duplicate(s)");
                    cleanedCount += deletedRows;

                    deleteStmt.close();
                }

                bestRs.close();
                findBestStmt.close();
            }

            rs.close();
            stmt.close();

            if (cleanedCount > 0) {
                System.out.println("✅ Cleanup complete! Removed " + cleanedCount + " duplicate high scores.");
            } else {
                System.out.println("✅ No duplicate high scores found. Database is clean!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error cleaning up high scores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Call this method ONCE in your initDatabase() method, right after
     * createAdminAccount()
     * 
     * Add this line to initDatabase():
     * cleanupDuplicateHighScores();
     */

    /**
     * Get high scores for a specific user
     */
    public ArrayList<Map<String, Object>> getUserHighScores(int userId) {
        ArrayList<Map<String, Object>> scores = new ArrayList<>();
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT level_id, score, stars, enemies_killed, achieved_at " +
                    "FROM high_scores WHERE user_id = ? ORDER BY score DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> score = new HashMap<>();
                score.put("level_id", rs.getInt("level_id"));
                score.put("score", rs.getInt("score"));
                score.put("stars", rs.getInt("stars"));
                score.put("enemies_killed", rs.getInt("enemies_killed"));
                score.put("achieved_at", rs.getString("achieved_at"));
                scores.add(score);
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to get user high scores: " + e.getMessage());
            e.printStackTrace();
        }
        return scores;
    }

    /**
     * Get all high scores (for admin view)
     */
    public ArrayList<Map<String, Object>> getAllHighScores() {
        ArrayList<Map<String, Object>> scores = new ArrayList<>();
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT u.username, hs.level_id, hs.score, hs.stars, hs.enemies_killed, hs.achieved_at " +
                    "FROM high_scores hs " +
                    "JOIN users u ON hs.user_id = u.id " +
                    "ORDER BY hs.score DESC LIMIT 100";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> score = new HashMap<>();
                score.put("username", rs.getString("username"));
                score.put("level_id", rs.getInt("level_id"));
                score.put("score", rs.getInt("score"));
                score.put("stars", rs.getInt("stars"));
                score.put("enemies_killed", rs.getInt("enemies_killed"));
                score.put("achieved_at", rs.getString("achieved_at"));
                scores.add(score);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to get high scores: " + e.getMessage());
            e.printStackTrace();
        }
        return scores;
    }

    /**
     * Get level completion statistics for admin panel
     */
    public Map<String, Object> getLevelCompletionStats(int levelId) {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT " +
                    "COUNT(*) as total_attempts, " +
                    "AVG(score) as avg_score, " +
                    "MAX(score) as max_score, " +
                    "AVG(stars) as avg_stars " +
                    "FROM high_scores WHERE level_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, levelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("total_attempts", rs.getInt("total_attempts"));
                stats.put("avg_score", rs.getDouble("avg_score"));
                stats.put("max_score", rs.getInt("max_score"));
                stats.put("avg_stars", rs.getDouble("avg_stars"));
                rs.close();
                pstmt.close();
                return stats;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to get level stats: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get total levels count
     */
    public int getTotalLevels() {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT COUNT(*) FROM levels WHERE is_active = 1";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get total levels: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get total users count
     */
    public int getTotalUsers() {
        try {
            // Check connection first
            if (!isConnectionValid()) {
                reconnect();
            }

            String sql = "SELECT COUNT(*) FROM users WHERE is_admin = 0";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get total users: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Check if database connection is still valid
     */
    public boolean isConnectionValid() {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
            return connection.isValid(2); // 2 second timeout
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Reconnect if connection is lost
     */
    public void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ Database reconnected successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Failed to reconnect to database: " + e.getMessage());
        }
    }

    /**
     * Create a backup of the database (for admin purposes)
     */
    public boolean createBackup(String backupPath) {
        try {
            String backupCommand = "mysqldump -u " + DB_USER + " -h " + DB_HOST + " -P " + DB_PORT + " " + DB_NAME
                    + " > " + backupPath;

            Process process = Runtime.getRuntime().exec(backupCommand);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Database backup created: " + backupPath);
                return true;
            } else {
                System.err.println("❌ Database backup failed with exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Database backup error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection: " + e.getMessage());
        }
    }
}