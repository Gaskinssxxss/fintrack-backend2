import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private final FileManager fileManager;
    private final List<Transaction> transactions;

    public TransactionService() {
        this.fileManager = new FileManager();
        this.transactions = new ArrayList<>(fileManager.loadTransactions());
    }

    // ====== CREATE ======

    public synchronized Transaction addTransaction(String username, String date,
                                                   String description, String category,
                                                   String type, double amount) {

        String id = UUID.randomUUID().toString();
        Transaction t = new Transaction(id, username, date, description, category, type, amount);

        transactions.add(t);
        fileManager.saveTransaction(t);

        return t;
    }

    // Format command: ADD|username|date|description|category|type|amount
    public synchronized Transaction addTransactionFromCommand(String commandLine) {
        String[] parts = commandLine.split("\\|");
        if (parts.length != 7) {
            throw new IllegalArgumentException("Format ADD tidak valid: " + commandLine);
        }

        String username    = parts[1];
        String date        = parts[2];
        String description = parts[3];
        String category    = parts[4];
        String type        = parts[5];
        double amount      = Double.parseDouble(parts[6]);

        return addTransaction(username, date, description, category, type, amount);
    }

    // ====== READ ======

    public synchronized List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public synchronized FinancialSummary getSummary() {
        double income = 0.0;
        double expense = 0.0;

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                income += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                expense += t.getAmount();
            }
        }

        return new FinancialSummary(income, expense);
    }

    // Format: DATA_ALL|id;date;description;category;type;amount#id2;...
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
              .append(t.getAmount());

            if (i < transactions.size() - 1) {
                sb.append("#");
            }
        }

        return sb.toString();
    }

    // ====== UPDATE ======

    // Format command: UPDATE|id|username|date|description|category|type|amount
    public synchronized boolean updateTransactionFromCommand(String commandLine) {
        String[] parts = commandLine.split("\\|");
        if (parts.length != 8) {
            throw new IllegalArgumentException("Format UPDATE tidak valid: " + commandLine);
        }

        String id          = parts[1];
        String username    = parts[2];
        String date        = parts[3];
        String description = parts[4];
        String category    = parts[5];
        String type        = parts[6];
        double amount      = Double.parseDouble(parts[7]);

        return updateTransaction(id, username, date, description, category, type, amount);
    }

    public synchronized boolean updateTransaction(String id,
                                                  String username,
                                                  String date,
                                                  String description,
                                                  String category,
                                                  String type,
                                                  double amount) {
        for (Transaction t : transactions) {
            if (t.getId().equals(id)) {
                t.setUsername(username);
                t.setDate(date);
                t.setDescription(description);
                t.setCategory(category);
                t.setType(type);
                t.setAmount(amount);

                fileManager.saveAll(transactions);
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
        boolean removed = transactions.removeIf(t -> t.getId().equals(id));
        if (removed) {
            fileManager.saveAll(transactions);
        }
        return removed;
    }
}
