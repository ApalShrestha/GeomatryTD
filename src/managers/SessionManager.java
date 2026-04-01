package managers;

/**
 * Manages user session data and authentication state
 * Singleton pattern to maintain single session throughout the application
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private int userId;
    private String username;
    private String email;
    private boolean isAdmin;
    private boolean isLoggedIn;
    
    private SessionManager() {
        this.isLoggedIn = false;
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Start a new user session
     */
    public void login(int userId, String username, String email, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isLoggedIn = true;
        
        System.out.println("Session started for: " + username + (isAdmin ? " (Admin)" : " (Player)"));
    }
    
    /**
     * End current session
     */
    public void logout() {
        System.out.println("Session ended for: " + username);
        this.userId = -1;
        this.username = null;
        this.email = null;
        this.isAdmin = false;
        this.isLoggedIn = false;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return isLoggedIn && isAdmin;
    }
    
    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    
    /**
     * Reset session (for testing or logout)
     */
    public void reset() {
        logout();
    }
}
