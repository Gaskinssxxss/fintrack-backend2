import java.util.List;

public class TestAccountService {
    public static void main(String[] args) {
        System.out.println("=== Testing AccountService ===\n");
        
        service.AccountService service = new service.AccountService();
        
        // Test 1: Add accounts
        System.out.println("Test 1: Adding accounts...");
        String id1 = service.addAccount("BCA Savings", "1234567890", 5000000L, "Bank");
        System.out.println("Added account with ID: " + id1);
        
        String id2 = service.addAccount("GoPay", "081234567890", 250000L, "Dompet Digital");
        System.out.println("Added account with ID: " + id2);
        
        String id3 = service.addAccount("Cash Wallet", "-", 100000L, "Cash");
        System.out.println("Added account with ID: " + id3);
        
        // Test 2: Get all accounts
        System.out.println("\nTest 2: Getting all accounts...");
        java.util.List accounts = service.getAllAccounts();
        System.out.println("Total accounts: " + accounts.size());
        for (Object obj : accounts) {
            model.Account acc = (model.Account) obj;
            System.out.println("  - " + acc.getName() + " (" + acc.getType() + "): " + acc.getBalance());
        }
        
        // Test 3: Get account by ID
        System.out.println("\nTest 3: Getting account by ID...");
        model.Account found = service.getAccountById(id1);
        if (found != null) {
            System.out.println("Found: " + found.getName() + " - Balance: " + found.getBalance());
        } else {
            System.out.println("Account not found!");
        }
        
        // Test 4: Update account
        System.out.println("\nTest 4: Updating account...");
        boolean updated = service.updateAccount(id1, "BCA Savings Updated", "1234567890", 6000000L, "Bank");
        System.out.println("Update result: " + updated);
        
        model.Account updatedAccount = service.getAccountById(id1);
        if (updatedAccount != null) {
            System.out.println("Updated account: " + updatedAccount.getName() + " - Balance: " + updatedAccount.getBalance());
        }
        
        // Test 5: Delete account
        System.out.println("\nTest 5: Deleting account...");
        boolean deleted = service.deleteAccount(id3);
        System.out.println("Delete result: " + deleted);
        
        accounts = service.getAllAccounts();
        System.out.println("Remaining accounts: " + accounts.size());
        
        // Test 6: Try to get deleted account
        System.out.println("\nTest 6: Trying to get deleted account...");
        model.Account deletedAccount = service.getAccountById(id3);
        System.out.println("Deleted account found: " + (deletedAccount != null));
        
        // Test 7: Try to update non-existent account
        System.out.println("\nTest 7: Trying to update non-existent account...");
        boolean updateNonExistent = service.updateAccount("fake-id", "Fake", "000", 0L, "Cash");
        System.out.println("Update non-existent result: " + updateNonExistent);
        
        // Test 8: Try to delete non-existent account
        System.out.println("\nTest 8: Trying to delete non-existent account...");
        boolean deleteNonExistent = service.deleteAccount("fake-id");
        System.out.println("Delete non-existent result: " + deleteNonExistent);
        
        System.out.println("\n=== All tests completed ===");
    }
}
