package model;

import java.io.Serializable;

public class Session implements Serializable {

    // Session timeout: 30 minutes in milliseconds
    public static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    private String sessionToken;      // UUID for session token
    private String username;          // Username that is logged in
    private long createdAt;           // Timestamp when session was created (milliseconds)
    private long lastAccessedAt;      // Timestamp of last access (milliseconds)

    public Session(String sessionToken, String username, long createdAt, long lastAccessedAt) {
        this.sessionToken = sessionToken;
        this.username = username;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
    }

    /**
     * Check if session has expired based on SESSION_TIMEOUT
     * @return true if session has timed out, false otherwise
     */
    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastAccessedAt) > SESSION_TIMEOUT;
    }

    // Getters
    public String getSessionToken() {
        return sessionToken;
    }

    public String getUsername() {
        return username;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastAccessedAt() {
        return lastAccessedAt;
    }

    // Setters
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastAccessedAt(long lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
}
