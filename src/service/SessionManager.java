package service;

import model.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    // Thread-safe map for session storage
    private final ConcurrentHashMap<String, Session> sessions;
    
    // Background cleanup thread
    private final ScheduledExecutorService cleanupScheduler;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        
        // Initialize background cleanup thread
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SessionCleanupThread");
            thread.setDaemon(true); // Daemon thread won't prevent JVM shutdown
            return thread;
        });
        
        // Schedule cleanup to run every 5 minutes
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanupExpiredSessions,
            5, // Initial delay
            5, // Period
            TimeUnit.MINUTES
        );
    }

    /**
     * Create a new session for a user
     * @param username The username to create session for
     * @return The generated session token (UUID)
     */
    public synchronized String createSession(String username) {
        String sessionToken = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        Session session = new Session(sessionToken, username, currentTime, currentTime);
        sessions.put(sessionToken, session);
        return sessionToken;
    }

    /**
     * Validate if a session token is valid and not expired
     * @param sessionToken The session token to validate
     * @return true if valid and not expired, false otherwise
     */
    public synchronized boolean validateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }

        Session session = sessions.get(sessionToken);
        if (session == null) {
            return false;
        }

        // Check if session has expired
        if (session.isExpired()) {
            sessions.remove(sessionToken);
            return false;
        }

        return true;
    }

    /**
     * Get username from a session token
     * @param sessionToken The session token
     * @return The username associated with the token, or null if invalid
     */
    public synchronized String getUsernameFromSession(String sessionToken) {
        Session session = sessions.get(sessionToken);
        if (session != null && !session.isExpired()) {
            return session.getUsername();
        }
        return null;
    }

    /**
     * Update the last accessed time for a session
     * @param sessionToken The session token to update
     */
    public synchronized void updateLastAccessed(String sessionToken) {
        Session session = sessions.get(sessionToken);
        if (session != null) {
            session.setLastAccessedAt(System.currentTimeMillis());
        }
    }

    /**
     * Remove a session (for logout)
     * @param sessionToken The session token to remove
     */
    public synchronized void removeSession(String sessionToken) {
        sessions.remove(sessionToken);
    }

    /**
     * Clean up all expired sessions
     * This method should be called periodically by a background thread
     */
    public synchronized void cleanupExpiredSessions() {
        List<String> expiredTokens = new ArrayList<>();
        
        // Find all expired sessions
        for (String token : sessions.keySet()) {
            Session session = sessions.get(token);
            if (session != null && session.isExpired()) {
                expiredTokens.add(token);
            }
        }
        
        // Remove expired sessions
        for (String token : expiredTokens) {
            sessions.remove(token);
        }
    }

    /**
     * Get all active sessions for a specific username
     * Used for multi-device support
     * @param username The username to get sessions for
     * @return List of active sessions for the user
     */
    public synchronized List<Session> getActiveSessions(String username) {
        List<Session> userSessions = new ArrayList<>();
        
        for (Session session : sessions.values()) {
            if (session.getUsername().equals(username) && !session.isExpired()) {
                userSessions.add(session);
            }
        }
        
        return userSessions;
    }
}
