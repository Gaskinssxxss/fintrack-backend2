/**
 * Test authenticated commands with valid session tokens
 */
public class TestAuthenticatedCommands {

    public static void main(String[] args) {
        System.out.println("=== Testing Authenticated Commands with Session ===\n");
        
        Backend2Facade facade = new Backend2Facade();
        
        // Register and login to get a session token
        facade.handleTextCommand("REGISTER|authuser|pass123");
        String loginResult = facade.handleTextCommand("LOGIN|authuser|pass123");
        String sessionToken = loginResult.split("\\|")[1];
        
        System.out.println("Session token: " + sessionToken + "\n");
        
        testAccountCommands(facade, sessionToken);
        testCategoryCommands(facade, sessionToken);
        testTransactionCommands(facade, sessionToken);
        testSummaryCommands(facade, sessionToken);
        
        System.out.println("\n=== All Authenticated Command Tests Completed ===");
    }
    
    private static void testAccountCommands(Backend2Facade facade, String token) {
        System.out.println("Test 1: Account Commands with Session");
        
        // Add account
        String result = facade.handleTextCommand("ADD_ACCOUNT|" + token + "|BCA Savings|1234567890|5000000|Bank");
        System.out.println("ADD_ACCOUNT: " + result);
        assert result.startsWith("OK|") : "Should successfully add account";
        String accountId = result.split("\\|")[1];
        
        // Get accounts
        result = facade.handleTextCommand("GET_ACCOUNTS|" + token);
        System.out.println("GET_ACCOUNTS: " + result);
        assert result.startsWith("DATA_ACCOUNTS|") : "Should return accounts data";
        assert result.contains(accountId) : "Should contain the added account";
        
        // Update account
        result = facade.handleTextCommand("UPDATE_ACCOUNT|" + token + "|" + accountId + "|BCA Updated|1234567890|6000000|Bank");
        System.out.println("UPDATE_ACCOUNT: " + result);
        assert "OK".equals(result) : "Should successfully update account";
        
        // Delete account
        result = facade.handleTextCommand("DELETE_ACCOUNT|" + token + "|" + accountId);
        System.out.println("DELETE_ACCOUNT: " + result);
        assert "OK".equals(result) : "Should successfully delete account";
        
        System.out.println("✓ Account commands test passed\n");
    }
    
    private static void testCategoryCommands(Backend2Facade facade, String token) {
        System.out.println("Test 2: Category Commands with Session");
        
        // Add category
        String result = facade.handleTextCommand("ADD_CATEGORY|" + token + "|Pengeluaran|Test Category");
        System.out.println("ADD_CATEGORY: " + result);
        assert "OK".equals(result) : "Should successfully add category";
        
        // Get categories
        result = facade.handleTextCommand("GET_CATEGORIES|" + token);
        System.out.println("GET_CATEGORIES: " + result);
        assert result.startsWith("DATA_CATEGORIES|") : "Should return categories data";
        assert result.contains("Test Category") : "Should contain the added category";
        
        // Delete category
        result = facade.handleTextCommand("DELETE_CATEGORY|" + token + "|Pengeluaran|Test Category");
        System.out.println("DELETE_CATEGORY: " + result);
        assert "OK".equals(result) : "Should successfully delete category";
        
        System.out.println("✓ Category commands test passed\n");
    }
    
    private static void testTransactionCommands(Backend2Facade facade, String token) {
        System.out.println("Test 3: Transaction Commands with Session");
        
        // Add transaction
        String result = facade.handleTextCommand("ADD|" + token + "|2025-12-01|Test Transaction|Makanan|EXPENSE|50000|BCA|Bank");
        System.out.println("ADD: " + result);
        assert result.startsWith("SUMMARY|") : "Should return summary after adding transaction";
        
        // Get all transactions
        result = facade.handleTextCommand("GET_ALL|" + token);
        System.out.println("GET_ALL: " + result);
        assert result.startsWith("DATA_ALL|") : "Should return all transactions";
        
        // Get summary
        result = facade.handleTextCommand("GET_SUMMARY|" + token);
        System.out.println("GET_SUMMARY: " + result);
        assert result.startsWith("SUMMARY|") : "Should return summary";
        
        System.out.println("✓ Transaction commands test passed\n");
    }
    
    private static void testSummaryCommands(Backend2Facade facade, String token) {
        System.out.println("Test 4: Summary Commands with Session");
        
        // Get monthly summary
        String result = facade.handleTextCommand("GET_MONTHLY_SUMMARY|" + token + "|2025-12");
        System.out.println("GET_MONTHLY_SUMMARY: " + result);
        assert result.startsWith("MONTHLY_SUMMARY|") : "Should return monthly summary";
        
        // Get category breakdown
        result = facade.handleTextCommand("GET_CATEGORY_BREAKDOWN|" + token + "|2025-12");
        System.out.println("GET_CATEGORY_BREAKDOWN: " + result);
        assert result.startsWith("CATEGORY_BREAKDOWN|") : "Should return category breakdown";
        
        // Filter transactions
        result = facade.handleTextCommand("FILTER_TRANSACTIONS|" + token + "|||");
        System.out.println("FILTER_TRANSACTIONS: " + result);
        assert result.startsWith("DATA_ALL|") : "Should return filtered transactions";
        
        System.out.println("✓ Summary commands test passed\n");
    }
}
