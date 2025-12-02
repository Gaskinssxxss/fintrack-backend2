import model.Transaction;
import model.Account;
import persistence.FileManager;
import persistence.AccountFileManager;
import java.util.*;

/**
 * One-time utility to recalculate account balances based on ALL transactions in
 * database.
 * This is needed after adding automatic balance adjustment feature.
 */
public class RecalculateBalances {
    public static void main(String[] args) {
        FileManager fm = new FileManager();
        AccountFileManager afm = new AccountFileManager();

        List<Transaction> transactions = fm.loadTransactions();
        List<Account> accounts = afm.loadAccounts();

        // Create a map to track balance adjustments per account
        Map<String, Long> balanceAdjustments = new HashMap<>();

        System.out.println("=== Analyzing Transactions ===");
        for (Transaction t : transactions) {
            String key = t.getUsername() + "|" + t.getAccountName() + "|" + t.getAccountType();
            long delta = "INCOME".equalsIgnoreCase(t.getType()) ? t.getAmount() : -t.getAmount();

            balanceAdjustments.put(key, balanceAdjustments.getOrDefault(key, 0L) + delta);

            System.out.println("Transaction: " + t.getDescription() +
                    " | Type: " + t.getType() +
                    " | Amount: " + t.getAmount() +
                    " | Account: " + t.getAccountName() +
                    " | Delta: " + delta);
        }

        System.out.println("\n=== Current Account Balances ===");
        for (Account acc : accounts) {
            System.out.println("Account: " + acc.getName() +
                    " | Username: " + acc.getUsername() +
                    " | Type: " + acc.getType() +
                    " | Balance: " + acc.getBalance());
        }

        System.out.println("\n=== Required Balance Adjustments ===");
        for (Map.Entry<String, Long> entry : balanceAdjustments.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        System.out.println("\n=== Applying Adjustments ===");
        List<Account> updatedAccounts = new ArrayList<>();
        for (Account acc : accounts) {
            String key = acc.getUsername() + "|" + acc.getName() + "|" + acc.getType();
            long adjustment = balanceAdjustments.getOrDefault(key, 0L);
            long newBalance = acc.getBalance() + adjustment;

            Account updated = new Account(
                    acc.getId(),
                    acc.getUsername(),
                    acc.getName(),
                    acc.getNumber(),
                    newBalance,
                    acc.getType());
            updatedAccounts.add(updated);

            System.out.println("Account: " + acc.getName() +
                    " | Old Balance: " + acc.getBalance() +
                    " | Adjustment: " + adjustment +
                    " | New Balance: " + newBalance);
        }

        // Save updated balances
        afm.saveAccounts(updatedAccounts);
        System.out.println("\n✓ Account balances updated successfully!");
    }
}
