/**
 * Test class for session-related commands in Backend2Facade
 */
public class TestBackend2FacadeSessionSimple {

    public static void main(String[] args) {
        System.out.println("=== Testing Backend2Facade Session Commands ===\n");
        
        Backend2Facade facade = new Backend2Facade();
        
        testLoginWithSession(facade);
        testLogout(facade);
        testValidateSession(facade);
        testAuthenticatedCommandsRequireSession(facade);
        testInvalidSessionHandling(facade);
        
        System.out.println("\n=== All Backend2Facade Session Tests Completed ===");
    }
    
    private static void testLoginWithSession(Backend2Facade facade) {
        System.out.println("Test 1: LOGIN returns session token");
        
        // First register a user
        String registerResult = facade.handleTextCommand("REGISTER|sessiontestuser|password123");
        System.out.println("Register result: " + registerResult);
        
        // Login and get session token
        String loginResult = facade.handleTextCommand("LOGIN|sessiontestuser|password123");
        System.out.println("Login result: " + loginResult);
        
        assert loginResult.startsWith("OK|") : "Login should return OK with session token";
        String[] parts = loginResult.split("\\|");
        assert parts.length == 2 : "Login response should have 2 parts";
        
        String sessionToken = parts[1];
        assert sessionToken != null && !sessionToken.isEmpty() : "Session token should not be empty";
        
        System.out.println("✓ Login with session test passed\n");
    }
    
    private static void testLogout(Backend2Facade facade) {
        System.out.println("Test 2: LOGOUT removes session");
        
        // Login to get a session token
        String loginResult = facade.handleTextCommand("LOGIN|sessiontestuser|password123");
        String sessionToken = loginResult.split("\\|")[1];
        
        // Logout
        String logoutResult = facade.handleTextCommand("LOGOUT|" + sessionToken);
        System.out.println("Logout result: " + logoutResult);
        assert "OK".equals(logoutResult) : "Logout should return OK";
        
        // Try to validate the session after logout
        String validateResult = facade.handleTextCommand("VALIDATE_SESSION|" + sessionToken);
        System.out.println("Validate after logout: " + validateResult);
        assert validateResult.startsWith("ERROR|SESSION_INVALID") : "Session should be invalid after logout";
        
        System.out.println("✓ Logout test passed\n");
    }
    
    private static void testValidateSession(Backend2Facade facade) {
        System.out.println("Test 3: VALIDATE_SESSION command");
        
        // Login to get a session token
        String loginResult = facade.handleTextCommand("LOGIN|sessiontestuser|password123");
        String sessionToken = loginResult.split("\\|")[1];
        
        // Validate the session
        String validateResult = facade.handleTextCommand("VALIDATE_SESSION|" + sessionToken);
        System.out.println("Validate result: " + validateResult);
        assert validateResult.startsWith("OK|") : "Validate should return OK with username";
        
        String[] parts = validateResult.split("\\|");
        assert parts.length == 2 : "Validate response should have 2 parts";
        assert "sessiontestuser".equals(parts[1]) : "Username should match";
        
        System.out.println("✓ Validate session test passed\n");
    }
    
    private static void testAuthenticatedCommandsRequireSession(Backend2Facade facade) {
        System.out.println("Test 4: Authenticated commands require session token");
        
        // Try GET_ALL without session token
        String result = facade.handleTextCommand("GET_ALL");
        System.out.println("GET_ALL without token: " + result);
        assert result.startsWith("ERROR|SESSION_REQUIRED") : "Should require session token";
        
        // Try GET_ACCOUNTS without session token
        result = facade.handleTextCommand("GET_ACCOUNTS");
        System.out.println("GET_ACCOUNTS without token: " + result);
        assert result.startsWith("ERROR|SESSION_REQUIRED") : "Should require session token";
        
        // Try GET_CATEGORIES without session token
        result = facade.handleTextCommand("GET_CATEGORIES");
        System.out.println("GET_CATEGORIES without token: " + result);
        assert result.startsWith("ERROR|SESSION_REQUIRED") : "Should require session token";
        
        System.out.println("✓ Authenticated commands require session test passed\n");
    }
    
    private static void testInvalidSessionHandling(Backend2Facade facade) {
        System.out.println("Test 5: Invalid session token handling");
        
        String fakeToken = "invalid-token-12345";
        
        // Try GET_ALL with invalid token
        String result = facade.handleTextCommand("GET_ALL|" + fakeToken);
        System.out.println("GET_ALL with invalid token: " + result);
        assert result.startsWith("ERROR|SESSION_INVALID") : "Should return SESSION_INVALID error";
        
        // Try ADD_ACCOUNT with invalid token
        result = facade.handleTextCommand("ADD_ACCOUNT|" + fakeToken + "|BCA|123456|1000000|Bank");
        System.out.println("ADD_ACCOUNT with invalid token: " + result);
        assert result.startsWith("ERROR|SESSION_INVALID") : "Should return SESSION_INVALID error";
        
        System.out.println("✓ Invalid session handling test passed\n");
    }
}
