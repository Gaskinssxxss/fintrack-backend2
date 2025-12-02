package service;

import model.FinancialSummary;
import model.Transaction;
import persistence.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private final FileManager fileManager;
    private final List<Transaction> transactions;
    private final AccountService accountService;

    public TransactionService(AccountService accountService) {
        this.fileManager = new FileManager();
        this.transactions = new ArrayList<>(fileManager.loadTransactions());
        this.accountService = accountService;
    }

    // ====== CREATE ======

    public synchronized Transaction addTransaction(String username, String date,
            String description, String category,
            String type, long amount,
            String accountName, String accountType) {

        String id = UUID.randomUUID().toString();
        Transaction t = new Transaction(id, username, date, description, category, type, amount, accountName,
                accountType);

        transactions.add(t);
        fileManager.saveTransaction(t);

        // Update account balance
        if (accountService != null && !accountName.equals("-")) {
            long delta = "INCOME".equalsIgnoreCase(type) ? amount : -amount;
            accountService.adjustBalance(username, accountName, accountType, delta);
        }

        return t;
    }

    // Format command:
    // ADD|username|date|description|category|type|amount|accountName|accountType
    // Also supports old format: ADD|username|date|description|category|type|amount
    // (backward compatibility)
    public synchronized Transaction addTransactionFromCommand(String commandLine) {
        String[] parts = commandLine.split("\\|");
        if (parts.length != 7 && parts.length != 9) {
            throw new IllegalArgumentException("Format ADD tidak valid: " + commandLine);
        }

        String username = parts[1];
        String date = parts[2];
        String description = parts[3];
        String category = parts[4];
        String type = parts[5];
        long amount = (long) Double.parseDouble(parts[6]);

        // Support backward compatibility with old format
        String accountName = (parts.length == 9) ? parts[7] : "-";
        String accountType = (parts.length == 9) ? parts[8] : "-";

        return addTransaction(username, date, description, category, type, amount, accountName, accountType);
    }

    // ====== READ ======

    public synchronized List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public synchronized FinancialSummary getSummary() {
        long income = 0;
        long expense = 0;

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                income += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                expense += t.getAmount();
            }
        }

        return new FinancialSummary(income, expense);
    }

    /**
     * Get financial summary for a specific user only.
     * 
     * @param username The username to filter by
     * @return FinancialSummary for the user
     */
    public synchronized FinancialSummary getSummaryForUser(String username) {
        long income = 0;
        long expense = 0;

        for (Transaction t : transactions) {
            if (t.getUsername().equals(username)) {
                if ("INCOME".equalsIgnoreCase(t.getType())) {
                    income += t.getAmount();
                } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                    expense += t.getAmount();
                }
            }
        }

        return new FinancialSummary(income, expense);
    }

    // Format:
    // DATA_ALL|id;date;description;category;type;amount;accountName;accountType#id2;...
    public synchronized String buildAllTransactionsMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("DATA_ALL|");

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            sb.append(t.getId()).append(";")
                    .append(t.getDate()).append(";")
                    .append(t.getDescription()).append(";")
                    .append(t.getCategory()).append(";")
                    .append(t.getType()).append(";")
                    .append(t.getAmount()).append(";")
                    .append(t.getAccountName()).append(";")
                    .append(t.getAccountType());

            if (i < transactions.size() - 1) {
                sb.append("#");
            }
        }

        return sb.toString();
    }

    // ====== UPDATE ======

    // Format command:
    // UPDATE|id|username|date|description|category|type|amount|accountName|accountType
    // Also supports old format:
    // UPDATE|id|username|date|description|category|type|amount (backward
    // compatibility)
    public synchronized boolean updateTransactionFromCommand(String commandLine) {
        String[] parts = commandLine.split("\\|");
        if (parts.length != 8 && parts.length != 10) {
            throw new IllegalArgumentException("Format UPDATE tidak valid: " + commandLine);
        }

        String id = parts[1];
        String username = parts[2];
        String date = parts[3];
        String description = parts[4];
        String category = parts[5];
        String type = parts[6];
        long amount = (long) Double.parseDouble(parts[7]);

        // Support backward compatibility with old format
        String accountName = (parts.length == 10) ? parts[8] : "-";
        String accountType = (parts.length == 10) ? parts[9] : "-";

        return updateTransaction(id, username, date, description, category, type, amount, accountName, accountType);
    }

    public synchronized boolean updateTransaction(String id,
            String username,
            String date,
            String description,
            String category,
            String type,
            long amount,
            String accountName,
            String accountType) {
        for (Transaction t : transactions) {
            if (t.getId().equals(id)) {
                // Reverse old transaction's balance effect
                if (accountService != null && !t.getAccountName().equals("-")) {
                    long oldDelta = "INCOME".equalsIgnoreCase(t.getType()) ? t.getAmount() : -t.getAmount();
                    accountService.adjustBalance(t.getUsername(), t.getAccountName(), t.getAccountType(), -oldDelta);
                }

                // Update transaction fields
                t.setUsername(username);
                t.setDate(date);
                t.setDescription(description);
                t.setCategory(category);
                t.setType(type);
                t.setAmount(amount);
                t.setAccountName(accountName);
                t.setAccountType(accountType);

                fileManager.saveAll(transactions);

                // Apply new transaction's balance effect
                if (accountService != null && !accountName.equals("-")) {
                    long newDelta = "INCOME".equalsIgnoreCase(type) ? amount : -amount;
                    accountService.adjustBalance(username, accountName, accountType, newDelta);
                }

                return true;
            }
        }
        return false;
    }

    // ====== DELETE ======

    // Format command: DELETE|id
    public synchronized boolean deleteTransactionFromCommand(String commandLine) {
        String[] parts = commandLine.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format DELETE tidak valid: " + commandLine);
        }
        String id = parts[1];
        return deleteTransaction(id);
    }

    public synchronized boolean deleteTransaction(String id) {
        // Find transaction to reverse its balance effect
        Transaction toDelete = null;
        for (Transaction t : transactions) {
            if (t.getId().equals(id)) {
                toDelete = t;
                break;
            }
        }

        boolean removed = transactions.removeIf(t -> t.getId().equals(id));
        if (removed) {
            fileManager.saveAll(transactions);

            // Reverse the transaction's balance effect
            if (toDelete != null && accountService != null && !toDelete.getAccountName().equals("-")) {
                long delta = "INCOME".equalsIgnoreCase(toDelete.getType()) ? toDelete.getAmount()
                        : -toDelete.getAmount();
                accountService.adjustBalance(toDelete.getUsername(), toDelete.getAccountName(),
                        toDelete.getAccountType(), -delta);
            }
        }
        return removed;
    }

    // ====== MONTHLY SUMMARY ======

    /**
     * Get monthly summary for a specific user and month.
     * Format: GET_MONTHLY_SUMMARY|username|yearMonth
     * Returns: MONTHLY_SUMMARY|yearMonth|income|expense|balance
     * 
     * @param username  The username to filter by
     * @param yearMonth The year-month in format YYYY-MM
     * @return Formatted response string
     */
    public synchronized String getMonthlySummary(String username, String yearMonth) {
        long income = 0;
        long expense = 0;

        // Filter transactions by username and yearMonth
        for (Transaction t : transactions) {
            if (t.getUsername().equals(username) && t.getDate().startsWith(yearMonth)) {
                if ("INCOME".equalsIgnoreCase(t.getType())) {
                    income += t.getAmount();
                } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                    expense += t.getAmount();
                }
            }
        }

        long balance = income - expense;
        return "MONTHLY_SUMMARY|" + yearMonth + "|" + income + "|" + expense + "|" + balance;
    }

    // ====== CATEGORY BREAKDOWN ======

    /**
     * Get category breakdown for expenses in a specific month.
     * Format: GET_CATEGORY_BREAKDOWN|username|yearMonth
     * Returns: CATEGORY_BREAKDOWN|count|cat1|amount1|cat2|amount2|...
     * 
     * @param username  The username to filter by
     * @param yearMonth The year-month in format YYYY-MM
     * @return Formatted response string
     */
    public synchronized String getCategoryBreakdown(String username, String yearMonth) {
        // Use a map to group by category and sum amounts
        java.util.Map<String, Long> categoryMap = new java.util.HashMap<>();

        // Filter transactions by username, yearMonth, and type=EXPENSE
        for (Transaction t : transactions) {
            if (t.getUsername().equals(username)
                    && t.getDate().startsWith(yearMonth)
                    && "EXPENSE".equalsIgnoreCase(t.getType())) {

                String category = t.getCategory();
                categoryMap.put(category, categoryMap.getOrDefault(category, 0L) + t.getAmount());
            }
        }

        // Build response
        StringBuilder sb = new StringBuilder();
        sb.append("CATEGORY_BREAKDOWN|").append(categoryMap.size());

        for (java.util.Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            sb.append("|").append(entry.getKey()).append("|").append(entry.getValue());
        }

        return sb.toString();
    }

    // ====== FILTER TRANSACTIONS ======

    /**
     * Filter transactions by optional criteria.
     * Format: FILTER_TRANSACTIONS|date|category|account
     * Returns: DATA_ALL format with filtered results
     * 
     * @param date     Optional date filter (empty string to skip)
     * @param category Optional category filter (empty string to skip)
     * @param account  Optional account filter (empty string to skip)
     * @return Formatted response string in DATA_ALL format
     */
    public synchronized String filterTransactions(String date, String category, String account) {
        List<Transaction> filtered = new ArrayList<>();

        for (Transaction t : transactions) {
            boolean matches = true;

            // Apply date filter if not empty
            if (date != null && !date.trim().isEmpty() && !t.getDate().equals(date)) {
                matches = false;
            }

            // Apply category filter if not empty
            if (category != null && !category.trim().isEmpty() && !t.getCategory().equals(category)) {
                matches = false;
            }

            // Apply account filter if not empty
            if (account != null && !account.trim().isEmpty() && !t.getAccountName().equals(account)) {
                matches = false;
            }

            if (matches) {
                filtered.add(t);
            }
        }

        // Build response in DATA_ALL format
        StringBuilder sb = new StringBuilder();
        sb.append("DATA_ALL|");

        for (int i = 0; i < filtered.size(); i++) {
            Transaction t = filtered.get(i);
            sb.append(t.getId()).append(";")
                    .append(t.getDate()).append(";")
                    .append(t.getDescription()).append(";")
                    .append(t.getCategory()).append(";")
                    .append(t.getType()).append(";")
                    .append(t.getAmount()).append(";")
                    .append(t.getAccountName()).append(";")
                    .append(t.getAccountType());

            if (i < filtered.size() - 1) {
                sb.append("#");
            }
        }

        return sb.toString();
    }
}
