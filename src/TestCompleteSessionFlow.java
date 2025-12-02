/**
 * Complete end-to-end test of session-based authentication flow
 */
public class TestCompleteSessionFlow {

    public static void main(String[] args) {
        System.out.println("=== Complete Session Flow Test ===\n");
        
        Backend2Facade facade = new Backend2Facade();
        
        // Step 1: Register a new user
        System.out.println("Step 1: Register new user");
        String result = facade.handleTextCommand("REGISTER|flowuser|securepass");
        System.out.println("Result: " + result);
        assert result.startsWith("REGISTER_OK") : "Registration should succeed";
        
        // Step 2: Login and receive session token
        System.out.println("\nStep 2: Login and receive session token");
        result = facade.handleTextCommand("LOGIN|flowuser|securepass");
        System.out.println("Result: " + result);
        assert result.startsWith("OK|") : "Login should succeed";
        String sessionToken = result.split("\\|")[1];
        System.out.println("Session Token: " + sessionToken);
        
        // Step 3: Validate the session
        System.out.println("\nStep 3: Validate session");
        result = facade.handleTextCommand("VALIDATE_SESSION|" + sessionToken);
        System.out.println("Result: " + result);
        assert result.equals("OK|flowuser") : "Session should be valid";
        
        // Step 4: Use session to perform authenticated operations
        System.out.println("\nStep 4: Perform authenticated operations");
        
        // Add an account
        result = facade.handleTextCommand("ADD_ACCOUNT|" + sessionToken + "|My Wallet|9999|100000|Cash");
        System.out.println("Add Account: " + result);
        assert result.startsWith("OK|") : "Should add account successfully";
        
        // Add a category
        result = facade.handleTextCommand("ADD_CATEGORY|" + sessionToken + "|Pengeluaran|Coffee");
        System.out.println("Add Category: " + result);
        assert result.equals("OK") : "Should add category successfully";
        
        // Add a transaction
        result = facade.handleTextCommand("ADD|" + sessionToken + "|2025-12-02|Morning Coffee|Coffee|EXPENSE|45000|My Wallet|Cash");
        System.out.println("Add Transaction: " + result);
        assert result.startsWith("SUMMARY|") : "Should add transaction successfully";
        
        // Get all transactions
        result = facade.handleTextCommand("GET_ALL|" + sessionToken);
        System.out.println("Get All Transactions: " + (result.length() > 100 ? result.substring(0, 100) + "..." : result));
        assert result.startsWith("DATA_ALL|") : "Should get transactions successfully";
        
        // Step 5: Try to use command without session (should fail)
        System.out.println("\nStep 5: Try command without session token");
        result = facade.handleTextCommand("GET_ACCOUNTS");
        System.out.println("Result: " + result);
        assert result.startsWith("ERROR|SESSION_REQUIRED") : "Should require session";
        
        // Step 6: Try to use invalid session token (should fail)
        System.out.println("\nStep 6: Try command with invalid session token");
        result = facade.handleTextCommand("GET_ACCOUNTS|invalid-token-xyz");
        System.out.println("Result: " + result);
        assert result.startsWith("ERROR|SESSION_INVALID") : "Should reject invalid session";
        
        // Step 7: Logout
        System.out.println("\nStep 7: Logout");
        result = facade.handleTextCommand("LOGOUT|" + sessionToken);
        System.out.println("Result: " + result);
        assert result.equals("OK") : "Logout should succeed";
        
        // Step 8: Try to use session after logout (should fail)
        System.out.println("\nStep 8: Try to use session after logout");
        result = facade.handleTextCommand("GET_ACCOUNTS|" + sessionToken);
        System.out.println("Result: " + result);
        assert result.startsWith("ERROR|SESSION_INVALID") : "Session should be invalid after logout";
        
        // Step 9: Login again (multi-device support)
        System.out.println("\nStep 9: Login again (simulating another device)");
        result = facade.handleTextCommand("LOGIN|flowuser|securepass");
        System.out.println("Result: " + result);
        assert result.startsWith("OK|") : "Should be able to login again";
        String newSessionToken = result.split("\\|")[1];
        System.out.println("New Session Token: " + newSessionToken);
        assert !newSessionToken.equals(sessionToken) : "New session should have different token";
        
        // Step 10: Use new session
        System.out.println("\nStep 10: Use new session");
        result = facade.handleTextCommand("GET_ACCOUNTS|" + newSessionToken);
        System.out.println("Result: " + (result.length() > 100 ? result.substring(0, 100) + "..." : result));
        assert result.startsWith("DATA_ACCOUNTS|") : "New session should work";
        
        System.out.println("\n=== ✓ Complete Session Flow Test PASSED ===");
        System.out.println("\nSummary:");
        System.out.println("- User registration: ✓");
        System.out.println("- Login with session token: ✓");
        System.out.println("- Session validation: ✓");
        System.out.println("- Authenticated operations: ✓");
        System.out.println("- Session required enforcement: ✓");
        System.out.println("- Invalid session rejection: ✓");
        System.out.println("- Logout: ✓");
        System.out.println("- Session invalidation after logout: ✓");
        System.out.println("- Multi-device support: ✓");
    }
}
