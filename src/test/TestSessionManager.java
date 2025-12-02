package test;

import service.SessionManager;
import model.Session;
import java.util.List;

public class TestSessionManager {

    public static void main(String[] args) {
        System.out.println("=== Testing SessionManager ===\n");
        
        SessionManager sessionManager = new SessionManager();
        
        // Test 1: Create session
        System.out.println("Test 1: Create session");
        String token1 = sessionManager.createSession("user1");
        System.out.println("Created session token: " + token1);
        System.out.println("Token is valid: " + sessionManager.validateSession(token1));
        System.out.println();
        
        // Test 2: Get username from session
        System.out.println("Test 2: Get username from session");
        String username = sessionManager.getUsernameFromSession(token1);
        System.out.println("Username from token: " + username);
        System.out.println();
        
        // Test 3: Validate invalid session
        System.out.println("Test 3: Validate invalid session");
        boolean isValid = sessionManager.validateSession("invalid-token");
        System.out.println("Invalid token is valid: " + isValid);
        System.out.println();
        
        // Test 4: Multi-device support
        System.out.println("Test 4: Multi-device support");
        String token2 = sessionManager.createSession("user1");
        String token3 = sessionManager.createSession("user1");
        List<Session> activeSessions = sessionManager.getActiveSessions("user1");
        System.out.println("Active sessions for user1: " + activeSessions.size());
        System.out.println();
        
        // Test 5: Update last accessed
        System.out.println("Test 5: Update last accessed");
        sessionManager.updateLastAccessed(token1);
        System.out.println("Updated last accessed for token1");
        System.out.println("Token1 still valid: " + sessionManager.validateSession(token1));
        System.out.println();
        
        // Test 6: Remove session
        System.out.println("Test 6: Remove session");
        sessionManager.removeSession(token1);
        System.out.println("Removed token1");
        System.out.println("Token1 is valid after removal: " + sessionManager.validateSession(token1));
        System.out.println();
        
        // Test 7: Session expiration
        System.out.println("Test 7: Session expiration (simulated)");
        String token4 = sessionManager.createSession("user2");
        System.out.println("Created token4 for user2");
        System.out.println("Token4 is valid: " + sessionManager.validateSession(token4));
        System.out.println("Note: Actual expiration test requires waiting 30 minutes or modifying Session timeout");
        System.out.println();
        
        // Test 8: Cleanup expired sessions
        System.out.println("Test 8: Cleanup expired sessions");
        sessionManager.cleanupExpiredSessions();
        System.out.println("Cleanup completed (no expired sessions in this test)");
        System.out.println();
        
        System.out.println("=== All tests completed ===");
    }
}
