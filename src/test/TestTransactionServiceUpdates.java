package test;

import model.Transaction;
import service.TransactionService;
import java.util.List;

public class TestTransactionServiceUpdates {

    public static void main(String[] args) {
        System.out.println("=== Testing TransactionService Updates ===\n");
        
        TransactionService service = new TransactionService();
        
        // Test 1: Add transaction with new fields
        System.out.println("Test 1: Add transaction with account info");
        try {
            Transaction t1 = service.addTransactionFromCommand(
                "ADD|testuser|2025-11-18|Lunch|Makanan & Minuman|EXPENSE|50000|BCA Savings|Bank"
            );
            System.out.println("✓ Transaction added: " + t1.getId());
            System.out.println("  Account: " + t1.getAccountName() + " (" + t1.getAccountType() + ")");
            System.out.println("  Amount: " + t1.getAmount());
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Test 2: Add transaction with old format (backward compatibility)
        System.out.println("\nTest 2: Add transaction with old format");
        try {
            Transaction t2 = service.addTransactionFromCommand(
                "ADD|testuser|2025-11-18|Salary|Gaji|INCOME|5000000"
            );
            System.out.println("✓ Transaction added: " + t2.getId());
            System.out.println("  Account: " + t2.getAccountName() + " (" + t2.getAccountType() + ")");
            System.out.println("  Amount: " + t2.getAmount());
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Test 3: Get monthly summary
        System.out.println("\nTest 3: Get monthly summary");
        try {
            String summary = service.getMonthlySummary("testuser", "2025-11");
            System.out.println("✓ Monthly summary: " + summary);
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Test 4: Get category breakdown
        System.out.println("\nTest 4: Get category breakdown");
        try {
            String breakdown = service.getCategoryBreakdown("testuser", "2025-11");
            System.out.println("✓ Category breakdown: " + breakdown);
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Test 5: Filter transactions
        System.out.println("\nTest 5: Filter transactions by category");
        try {
            String filtered = service.filterTransactions("", "Makanan & Minuman", "");
            System.out.println("✓ Filtered transactions: " + filtered);
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Test 6: Build all transactions message
        System.out.println("\nTest 6: Build all transactions message");
        try {
            String allTransactions = service.buildAllTransactionsMessage();
            System.out.println("✓ All transactions message length: " + allTransactions.length());
            if (allTransactions.contains("BCA Savings")) {
                System.out.println("✓ Contains account information");
            }
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        System.out.println("\n=== All tests completed ===");
    }
}
