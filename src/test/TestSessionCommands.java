package test;

import service.SessionManager;

/**
 * Test class for session-related commands in Backend2Facade
 */
public class TestSessionCommands {

    public static void main(String[] args) {
        System.out.println("=== Testing Session Commands ===\n");
        
        testSessionManager();
        testSessionFlow();
        
        System.out.println("\n=== All Session Tests Completed ===");
    }
    
    private static void testSessionManager() {
        System.out.println("Test 1: SessionManager Basic Operations");
        SessionManager sm = new SessionManager();
        
        // Create session
        String token = sm.createSession("testuser");
        System.out.println("Created session token: " + token);
        assert token != null && !token.isEmpty() : "Session token should not be null or empty";
        
        // Validate session
        boolean valid = sm.validateSession(token);
        System.out.println("Session valid: " + valid);
        assert valid : "Session should be valid";
        
        // Get username
        String username = sm.getUsernameFromSession(token);
        System.out.println("Username from session: " + username);
        assert "testuser".equals(username) : "Username should match";
        
        // Update last accessed
        sm.updateLastAccessed(token);
        System.out.println("Updated last accessed time");
        
        // Remove session
        sm.removeSession(token);
        System.out.println("Removed session");
        
        // Validate after removal
        valid = sm.validateSession(token);
        System.out.println("Session valid after removal: " + valid);
        assert !valid : "Session should be invalid after removal";
        
        System.out.println("✓ SessionManager basic operations test passed\n");
    }
    
    private static void testSessionFlow() {
        System.out.println("Test 2: Multi-Session Flow");
        SessionManager sm = new SessionManager();
        
        // Create multiple sessions for same user
        String token1 = sm.createSession("user1");
        String token2 = sm.createSession("user1");
        
        System.out.println("Created two sessions for user1");
        System.out.println("Token 1: " + token1);
        System.out.println("Token 2: " + token2);
        
        assert !token1.equals(token2) : "Tokens should be unique";
        
        // Both should be valid
        assert sm.validateSession(token1) : "Token 1 should be valid";
        assert sm.validateSession(token2) : "Token 2 should be valid";
        
        // Remove one session
        sm.removeSession(token1);
        
        // Token 1 should be invalid, token 2 should still be valid
        assert !sm.validateSession(token1) : "Token 1 should be invalid after removal";
        assert sm.validateSession(token2) : "Token 2 should still be valid";
        
        System.out.println("✓ Multi-session flow test passed\n");
    }
}
