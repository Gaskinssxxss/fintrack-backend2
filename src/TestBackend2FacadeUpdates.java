/**
 * Test for Backend2Facade new command handlers
 * Tests Account, Category, and new Transaction commands
 */
public class TestBackend2FacadeUpdates {

    public static void main(String[] args) {
        Backend2Facade facade = new Backend2Facade();
        
        System.out.println("=== Testing Account Commands ===");
        
        // Test ADD_ACCOUNT
        String addAccountResult = facade.handleTextCommand("ADD_ACCOUNT|BCA Savings|1234567890|5000000|Bank");
        System.out.println("ADD_ACCOUNT: " + addAccountResult);
        assert addAccountResult.startsWith("OK|") : "ADD_ACCOUNT should return OK with ID";
        
        // Extract account ID from response
        String accountId = addAccountResult.split("\\|")[1];
        
        // Test GET_ACCOUNTS
        String getAccountsResult = facade.handleTextCommand("GET_ACCOUNTS");
        System.out.println("GET_ACCOUNTS: " + getAccountsResult);
        assert getAccountsResult.startsWith("DATA_ACCOUNTS|") : "GET_ACCOUNTS should return DATA_ACCOUNTS";
        assert getAccountsResult.contains("BCA Savings") : "Should contain added account";
        
        // Test UPDATE_ACCOUNT
        String updateAccountResult = facade.handleTextCommand("UPDATE_ACCOUNT|" + accountId + "|BCA Premium|1234567890|6000000|Bank");
        System.out.println("UPDATE_ACCOUNT: " + updateAccountResult);
        assert updateAccountResult.equals("OK") : "UPDATE_ACCOUNT should return OK";
        
        // Test DELETE_ACCOUNT
        String deleteAccountResult = facade.handleTextCommand("DELETE_ACCOUNT|" + accountId);
        System.out.println("DELETE_ACCOUNT: " + deleteAccountResult);
        assert deleteAccountResult.equals("OK") : "DELETE_ACCOUNT should return OK";
        
        System.out.println("\n=== Testing Category Commands ===");
        
        // Test ADD_CATEGORY
        String addCategoryResult = facade.handleTextCommand("ADD_CATEGORY|Pengeluaran|Makanan & Minuman");
        System.out.println("ADD_CATEGORY: " + addCategoryResult);
        assert addCategoryResult.equals("OK") : "ADD_CATEGORY should return OK";
        
        // Test GET_CATEGORIES
        String getCategoriesResult = facade.handleTextCommand("GET_CATEGORIES");
        System.out.println("GET_CATEGORIES: " + getCategoriesResult);
        assert getCategoriesResult.startsWith("DATA_CATEGORIES|") : "GET_CATEGORIES should return DATA_CATEGORIES";
        
        // Test DELETE_CATEGORY
        String deleteCategoryResult = facade.handleTextCommand("DELETE_CATEGORY|Pengeluaran|Makanan & Minuman");
        System.out.println("DELETE_CATEGORY: " + deleteCategoryResult);
        assert deleteCategoryResult.equals("OK") : "DELETE_CATEGORY should return OK";
        
        System.out.println("\n=== Testing New Transaction Commands ===");
        
        // Add some test transactions first
        facade.handleTextCommand("ADD|testuser|2025-11-18|Lunch|Food|EXPENSE|50000|BCA|Bank");
        facade.handleTextCommand("ADD|testuser|2025-11-18|Salary|Income|INCOME|5000000|BCA|Bank");
        facade.handleTextCommand("ADD|testuser|2025-11-20|Dinner|Food|EXPENSE|75000|GoPay|Dompet Digital");
        
        // Test GET_MONTHLY_SUMMARY
        String monthlySummaryResult = facade.handleTextCommand("GET_MONTHLY_SUMMARY|testuser|2025-11");
        System.out.println("GET_MONTHLY_SUMMARY: " + monthlySummaryResult);
        assert monthlySummaryResult.startsWith("MONTHLY_SUMMARY|") : "GET_MONTHLY_SUMMARY should return MONTHLY_SUMMARY";
        
        // Test GET_CATEGORY_BREAKDOWN
        String categoryBreakdownResult = facade.handleTextCommand("GET_CATEGORY_BREAKDOWN|testuser|2025-11");
        System.out.println("GET_CATEGORY_BREAKDOWN: " + categoryBreakdownResult);
        assert categoryBreakdownResult.startsWith("CATEGORY_BREAKDOWN|") : "GET_CATEGORY_BREAKDOWN should return CATEGORY_BREAKDOWN";
        
        // Test FILTER_TRANSACTIONS (with date filter only, empty category and account)
        String filterResult = facade.handleTextCommand("FILTER_TRANSACTIONS|2025-11-18||");
        System.out.println("FILTER_TRANSACTIONS: " + filterResult);
        assert filterResult.startsWith("DATA_ALL|") : "FILTER_TRANSACTIONS should return DATA_ALL";
        
        System.out.println("\n=== Testing Updated Transaction Commands ===");
        
        // Test ADD with new format (with account info)
        String addWithAccountResult = facade.handleTextCommand("ADD|testuser|2025-12-01|Shopping|Shopping|EXPENSE|200000|BCA|Bank");
        System.out.println("ADD with account: " + addWithAccountResult);
        assert addWithAccountResult.startsWith("SUMMARY|") : "ADD should return SUMMARY";
        
        // Test ADD with old format (backward compatibility)
        String addOldFormatResult = facade.handleTextCommand("ADD|testuser|2025-12-01|Transport|Transport|EXPENSE|25000");
        System.out.println("ADD old format: " + addOldFormatResult);
        assert addOldFormatResult.startsWith("SUMMARY|") : "ADD old format should return SUMMARY";
        
        System.out.println("\n=== Testing Error Handling ===");
        
        // Test invalid format
        String invalidFormatResult = facade.handleTextCommand("ADD_ACCOUNT|OnlyName");
        System.out.println("Invalid format: " + invalidFormatResult);
        assert invalidFormatResult.startsWith("ERROR|INVALID_FORMAT") : "Should return INVALID_FORMAT error";
        
        // Test invalid amount
        String invalidAmountResult = facade.handleTextCommand("ADD_ACCOUNT|Test|123|notanumber|Bank");
        System.out.println("Invalid amount: " + invalidAmountResult);
        assert invalidAmountResult.startsWith("ERROR|INVALID_AMOUNT") : "Should return INVALID_AMOUNT error";
        
        // Test account not found
        String notFoundResult = facade.handleTextCommand("DELETE_ACCOUNT|nonexistent-id");
        System.out.println("Account not found: " + notFoundResult);
        assert notFoundResult.startsWith("ERROR|ACCOUNT_NOT_FOUND") : "Should return ACCOUNT_NOT_FOUND error";
        
        System.out.println("\n=== All Tests Passed! ===");
    }
}
