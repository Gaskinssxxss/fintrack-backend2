import service.TransactionService;
import service.ReportService;
import service.UserManager;
import service.AccountService;
import service.CategoryService;
import service.SessionManager;
import model.FinancialSummary;
import model.Account;
import model.Category;
import model.Transaction;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Backend2Facade {

    private final TransactionService transactionService;
    private final ReportService reportService;
    private final UserManager userManager;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final SessionManager sessionManager;

    public Backend2Facade() {
        this.accountService = new AccountService();
        this.transactionService = new TransactionService(accountService);
        this.reportService = new ReportService();
        this.userManager = new UserManager();
        this.categoryService = new CategoryService();
        this.sessionManager = new SessionManager();
    }

    // ====== API TEXT-BASED UNTUK BACKEND 1 (via string command) ======

    /**
     * Handle text-based commands from clients.
     * 
     * BREAKING CHANGE: All authenticated commands now require a session token as
     * the first parameter.
     * 
     * UNAUTHENTICATED COMMANDS (no session token required):
     * - REGISTER|username|password
     * - LOGIN|username|password -> Returns: OK|sessionToken
     * 
     * SESSION MANAGEMENT COMMANDS:
     * - LOGOUT|sessionToken -> Returns: OK
     * - VALIDATE_SESSION|sessionToken -> Returns: OK|username or
     * ERROR|SESSION_INVALID
     * 
     * AUTHENTICATED COMMANDS (require session token as first parameter):
     * 
     * Transaction Commands:
     * -
     * ADD|sessionToken|date|description|category|type|amount|accountName|accountType
     * -
     * UPDATE|sessionToken|id|date|description|category|type|amount|accountName|accountType
     * - DELETE|sessionToken|id
     * - GET_ALL|sessionToken
     * - GET_SUMMARY|sessionToken
     * - GET_MONTHLY_SUMMARY|sessionToken|yearMonth
     * - GET_CATEGORY_BREAKDOWN|sessionToken|yearMonth
     * - FILTER_TRANSACTIONS|sessionToken|date|category|account
     * 
     * Account Commands:
     * - ADD_ACCOUNT|sessionToken|name|number|balance|type
     * - GET_ACCOUNTS|sessionToken
     * - UPDATE_ACCOUNT|sessionToken|id|name|number|balance|type
     * - DELETE_ACCOUNT|sessionToken|id
     * 
     * Category Commands:
     * - ADD_CATEGORY|sessionToken|type|name
     * - GET_CATEGORIES|sessionToken
     * - DELETE_CATEGORY|sessionToken|type|name
     * 
     * Other Authenticated Commands:
     * - CHANGE_PASSWORD|sessionToken|oldPassword|newPassword
     * - REQ_REPORT|sessionToken
     * 
     * ERROR RESPONSES:
     * - ERROR|SESSION_REQUIRED - Session token is missing
     * - ERROR|SESSION_INVALID - Session token is invalid or expired
     * - ERROR|<error_code>|<description> - Other errors
     */
    public String handleTextCommand(String line) {
        if (line == null || line.isEmpty()) {
            return "ERROR|EMPTY_COMMAND";
        }

        // --- REGISTER ---
        // Format: REGISTER|username|password
        if (line.startsWith("REGISTER|")) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) {
                return "ERROR|REGISTER_FORMAT";
            }
            String username = parts[1];
            String password = parts[2];

            boolean ok = userManager.registerUser(username, password);
            if (ok) {
                return "REGISTER_OK|" + username;
            } else {
                return "ERROR|USER_EXISTS|" + username;
            }
        }

        // --- LOGIN ---
        // Format: LOGIN|username|password
        // Returns: OK|sessionToken on success
        if (line.startsWith("LOGIN|")) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) {
                System.out.println("[Backend2Facade] LOGIN: Invalid format, parts.length=" + parts.length);
                return "ERROR|LOGIN_FORMAT";
            }
            String username = parts[1];
            String password = parts[2];

            System.out.println(
                    "[Backend2Facade] LOGIN attempt: username='" + username + "', password='" + password + "'");

            boolean valid = userManager.validateLogin(username, password);
            System.out.println("[Backend2Facade] Login validation result: " + valid);

            if (valid) {
                // Create session and return session token
                String sessionToken = sessionManager.createSession(username);
                System.out.println("[Backend2Facade] Login successful, token=" + sessionToken);
                return "OK|" + sessionToken;
            } else {
                System.out.println("[Backend2Facade] Login failed for username: " + username);
                return "LOGIN_FAIL|INVALID_CREDENTIALS";
            }
        }

        // --- LOGOUT ---
        // Format: LOGOUT|sessionToken
        // Returns: OK on success
        if (line.startsWith("LOGOUT|")) {
            String[] parts = line.split("\\|");
            if (parts.length != 2) {
                return "ERROR|LOGOUT_FORMAT";
            }
            String sessionToken = parts[1];

            sessionManager.removeSession(sessionToken);
            return "OK";
        }

        // --- VALIDATE_SESSION ---
        // Format: VALIDATE_SESSION|sessionToken
        // Returns: OK|username on success, ERROR|SESSION_INVALID on failure
        if (line.startsWith("VALIDATE_SESSION|")) {
            String[] parts = line.split("\\|");
            if (parts.length != 2) {
                return "ERROR|VALIDATE_SESSION_FORMAT";
            }
            String sessionToken = parts[1];

            if (sessionManager.validateSession(sessionToken)) {
                String username = sessionManager.getUsernameFromSession(sessionToken);
                return "OK|" + username;
            } else {
                return "ERROR|SESSION_INVALID";
            }
        }

        // --- CHANGE PASSWORD ---
        // Format: CHANGE_PASSWORD|sessionToken|oldPassword|newPassword
        if (line.startsWith("CHANGE_PASSWORD|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 4) {
                return "ERROR|CHANGE_PASSWORD_FORMAT";
            }
            String oldPassword = parts[2];
            String newPassword = parts[3];

            boolean ok = userManager.changePassword(username, oldPassword, newPassword);
            if (ok) {
                return "CHANGE_PASSWORD_OK|" + username;
            } else {
                return "CHANGE_PASSWORD_FAIL|INVALID_CREDENTIALS";
            }
        }

        // --- ADD transaksi ---
        // Format:
        // ADD|sessionToken|date|description|category|type|amount|accountName|accountType
        // Also supports old format:
        // ADD|sessionToken|date|description|category|type|amount (backward
        // compatibility)
        if (line.startsWith("ADD|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            // Reconstruct command with username instead of sessionToken
            StringBuilder reconstructed = new StringBuilder("ADD|" + username);
            for (int i = 2; i < parts.length; i++) {
                reconstructed.append("|").append(parts[i]);
            }

            transactionService.addTransactionFromCommand(reconstructed.toString());
            FinancialSummary summary = transactionService.getSummary();
            return summary.toProtocolString(); // SUMMARY|...
        }

        // --- UPDATE transaksi ---
        // Format:
        // UPDATE|sessionToken|id|date|description|category|type|amount|accountName|accountType
        // Also supports old format:
        // UPDATE|sessionToken|id|date|description|category|type|amount (backward
        // compatibility)
        if (line.startsWith("UPDATE|")) {
            String[] parts = line.split("\\|");
            System.out.println("[Backend2Facade] UPDATE request received, parts.length=" + parts.length);

            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            System.out.println("[Backend2Facade] UPDATE: sessionToken=" + sessionToken + ", username=" + username);

            if (username == null) {
                System.out.println("[Backend2Facade] UPDATE: Session invalid");
                return "ERROR|SESSION_INVALID";
            }

            // Reconstruct command with username: UPDATE|id|username|date|...
            if (parts.length < 3) {
                return "ERROR|INVALID_FORMAT|UPDATE requires id and other fields";
            }

            StringBuilder reconstructed = new StringBuilder("UPDATE|" + parts[2] + "|" + username);
            for (int i = 3; i < parts.length; i++) {
                reconstructed.append("|").append(parts[i]);
            }

            System.out.println("[Backend2Facade] UPDATE: reconstructed command=" + reconstructed.toString());

            boolean updated = transactionService.updateTransactionFromCommand(reconstructed.toString());
            System.out.println("[Backend2Facade] UPDATE: result=" + updated);

            if (updated) {
                FinancialSummary summary = transactionService.getSummary();
                return summary.toProtocolString();
            } else {
                return "ERROR|UPDATE_NOT_FOUND";
            }
        }

        // --- DELETE transaksi ---
        // Format: DELETE|sessionToken|id
        if (line.startsWith("DELETE|")) {
            String[] parts = line.split("\\|");
            System.out.println("[Backend2Facade] DELETE request received, parts.length=" + parts.length);

            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            System.out.println("[Backend2Facade] DELETE: sessionToken=" + sessionToken + ", username=" + username);

            if (username == null) {
                System.out.println("[Backend2Facade] DELETE: Session invalid");
                return "ERROR|SESSION_INVALID";
            }

            // Reconstruct command: DELETE|id
            if (parts.length < 3) {
                return "ERROR|INVALID_FORMAT|DELETE requires id";
            }

            String reconstructed = "DELETE|" + parts[2];
            System.out.println("[Backend2Facade] DELETE: reconstructed command=" + reconstructed);

            boolean deleted = transactionService.deleteTransactionFromCommand(reconstructed);
            System.out.println("[Backend2Facade] DELETE: result=" + deleted);

            if (deleted) {
                FinancialSummary summary = transactionService.getSummary();
                return summary.toProtocolString();
            } else {
                return "ERROR|DELETE_NOT_FOUND";
            }
        }

        // --- GET_ALL transaksi ---
        // Format: GET_ALL|sessionToken
        // Returns:
        // DATA_ALL|count|id1|username1|date1|desc1|cat1|type1|amount1|accName1|accType1|...
        if (line.startsWith("GET_ALL|") || line.equals("GET_ALL")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            try {
                List<Transaction> allTransactions = transactionService.getAllTransactions();
                // Filter transactions by username
                List<Transaction> userTransactions = new java.util.ArrayList<>();
                for (Transaction t : allTransactions) {
                    if (t.getUsername().equals(username)) {
                        userTransactions.add(t);
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append("DATA_ALL|").append(userTransactions.size());

                for (Transaction t : userTransactions) {
                    sb.append("|").append(t.getId())
                            .append("|").append(t.getUsername())
                            .append("|").append(t.getDate())
                            .append("|").append(t.getDescription())
                            .append("|").append(t.getCategory())
                            .append("|").append(t.getType())
                            .append("|").append(t.getAmount())
                            .append("|").append(t.getAccountName() != null ? t.getAccountName() : "-")
                            .append("|").append(t.getAccountType() != null ? t.getAccountType() : "-");
                }

                return sb.toString();
            } catch (Exception e) {
                return "ERROR|GET_ALL_FAILED|" + e.getMessage();
            }
        }

        // --- ADD_ACCOUNT ---
        // Format: ADD_ACCOUNT|sessionToken|name|number|balance|type
        if (line.startsWith("ADD_ACCOUNT|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 6) {
                return "ERROR|INVALID_FORMAT|ADD_ACCOUNT requires sessionToken|name|number|balance|type";
            }
            try {
                String name = parts[2];
                String number = parts[3];
                long balance = Long.parseLong(parts[4]);
                String type = parts[5];

                String accountId = accountService.addAccount(username, name, number, balance, type);
                return "OK|" + accountId;
            } catch (NumberFormatException e) {
                return "ERROR|INVALID_AMOUNT|Balance must be a valid long integer";
            } catch (Exception e) {
                return "ERROR|ADD_ACCOUNT_FAILED|" + e.getMessage();
            }
        }

        // --- GET_ACCOUNTS ---
        // Format: GET_ACCOUNTS|sessionToken
        // Returns: DATA_ACCOUNTS|count|id1|name1|number1|balance1|type1|id2|...
        if (line.startsWith("GET_ACCOUNTS|") || line.equals("GET_ACCOUNTS")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            try {
                // Filter accounts by username
                List<Account> accounts = accountService.getAccountsByUsername(username);
                StringBuilder sb = new StringBuilder();
                sb.append("DATA_ACCOUNTS|").append(accounts.size());

                for (Account account : accounts) {
                    sb.append("|").append(account.getId())
                            .append("|").append(account.getName())
                            .append("|").append(account.getNumber())
                            .append("|").append(account.getBalance())
                            .append("|").append(account.getType());
                }

                return sb.toString();
            } catch (Exception e) {
                return "ERROR|GET_ACCOUNTS_FAILED|" + e.getMessage();
            }
        }

        // --- UPDATE_ACCOUNT ---
        // Format: UPDATE_ACCOUNT|sessionToken|id|name|number|balance|type
        if (line.startsWith("UPDATE_ACCOUNT|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 7) {
                return "ERROR|INVALID_FORMAT|UPDATE_ACCOUNT requires sessionToken|id|name|number|balance|type";
            }
            try {
                String id = parts[2];
                String name = parts[3];
                String number = parts[4];
                long balance = Long.parseLong(parts[5]);
                String type = parts[6];

                boolean updated = accountService.updateAccount(id, username, name, number, balance, type);
                if (updated) {
                    return "OK";
                } else {
                    return "ERROR|ACCOUNT_NOT_FOUND|Account with ID " + id + " does not exist";
                }
            } catch (NumberFormatException e) {
                return "ERROR|INVALID_AMOUNT|Balance must be a valid long integer";
            } catch (Exception e) {
                return "ERROR|UPDATE_ACCOUNT_FAILED|" + e.getMessage();
            }
        }

        // --- DELETE_ACCOUNT ---
        // Format: DELETE_ACCOUNT|sessionToken|id
        if (line.startsWith("DELETE_ACCOUNT|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 3) {
                return "ERROR|INVALID_FORMAT|DELETE_ACCOUNT requires sessionToken|id";
            }
            try {
                String id = parts[2];
                boolean deleted = accountService.deleteAccount(id);
                if (deleted) {
                    return "OK";
                } else {
                    return "ERROR|ACCOUNT_NOT_FOUND|Account with ID " + id + " does not exist";
                }
            } catch (Exception e) {
                return "ERROR|DELETE_ACCOUNT_FAILED|" + e.getMessage();
            }
        }

        // --- ADD_CATEGORY ---
        // Format: ADD_CATEGORY|sessionToken|type|name
        if (line.startsWith("ADD_CATEGORY|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 4) {
                return "ERROR|INVALID_FORMAT|ADD_CATEGORY requires sessionToken|type|name";
            }
            try {
                String type = parts[2];
                String name = parts[3];

                categoryService.addCategory(username, type, name);
                return "OK";
            } catch (Exception e) {
                return "ERROR|ADD_CATEGORY_FAILED|" + e.getMessage();
            }
        }

        // --- GET_CATEGORIES ---
        // Format: GET_CATEGORIES|sessionToken
        // Returns: DATA_CATEGORIES|count|type1|name1|type2|name2|...
        if (line.startsWith("GET_CATEGORIES|") || line.equals("GET_CATEGORIES")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            try {
                // Filter categories by username
                List<Category> categories = categoryService.getCategoriesByUsername(username);
                StringBuilder sb = new StringBuilder();
                sb.append("DATA_CATEGORIES|").append(categories.size());

                for (Category category : categories) {
                    sb.append("|").append(category.getType())
                            .append("|").append(category.getName());
                }

                return sb.toString();
            } catch (Exception e) {
                return "ERROR|GET_CATEGORIES_FAILED|" + e.getMessage();
            }
        }

        // --- DELETE_CATEGORY ---
        // Format: DELETE_CATEGORY|sessionToken|type|name
        if (line.startsWith("DELETE_CATEGORY|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 4) {
                return "ERROR|INVALID_FORMAT|DELETE_CATEGORY requires sessionToken|type|name";
            }
            try {
                String type = parts[2];
                String name = parts[3];

                boolean deleted = categoryService.deleteCategory(username, type, name);
                if (deleted) {
                    return "OK";
                } else {
                    return "ERROR|CATEGORY_NOT_FOUND|Category " + name + " of type " + type + " does not exist";
                }
            } catch (Exception e) {
                return "ERROR|DELETE_CATEGORY_FAILED|" + e.getMessage();
            }
        }

        // --- GET_SUMMARY untuk dashboard ---
        // Format: GET_SUMMARY|sessionToken
        if (line.startsWith("GET_SUMMARY")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            // Calculate summary for this user only
            FinancialSummary summary = transactionService.getSummaryForUser(username);
            return summary.toProtocolString();
        }

        // --- GET_MONTHLY_SUMMARY ---
        // Format: GET_MONTHLY_SUMMARY|sessionToken|yearMonth
        // Returns: MONTHLY_SUMMARY|yearMonth|income|expense|balance
        if (line.startsWith("GET_MONTHLY_SUMMARY|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 3) {
                return "ERROR|INVALID_FORMAT|GET_MONTHLY_SUMMARY requires sessionToken|yearMonth";
            }
            try {
                String yearMonth = parts[2];

                return transactionService.getMonthlySummary(username, yearMonth);
            } catch (Exception e) {
                return "ERROR|GET_MONTHLY_SUMMARY_FAILED|" + e.getMessage();
            }
        }

        // --- GET_CATEGORY_BREAKDOWN ---
        // Format: GET_CATEGORY_BREAKDOWN|sessionToken|yearMonth
        // Returns: CATEGORY_BREAKDOWN|count|cat1|amount1|cat2|amount2|...
        if (line.startsWith("GET_CATEGORY_BREAKDOWN|")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 3) {
                return "ERROR|INVALID_FORMAT|GET_CATEGORY_BREAKDOWN requires sessionToken|yearMonth";
            }
            try {
                String yearMonth = parts[2];

                return transactionService.getCategoryBreakdown(username, yearMonth);
            } catch (Exception e) {
                return "ERROR|GET_CATEGORY_BREAKDOWN_FAILED|" + e.getMessage();
            }
        }

        // --- FILTER_TRANSACTIONS ---
        // Format: FILTER_TRANSACTIONS|sessionToken|date|category|account
        // Returns: DATA_ALL format with filtered results
        if (line.startsWith("FILTER_TRANSACTIONS|")) {
            String[] parts = line.split("\\|", -1); // Use -1 to include trailing empty strings
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            if (parts.length != 5) {
                return "ERROR|INVALID_FORMAT|FILTER_TRANSACTIONS requires sessionToken|date|category|account (got "
                        + parts.length + " parts)";
            }
            try {
                String date = parts[2];
                String category = parts[3];
                String account = parts[4];

                return transactionService.filterTransactions(date, category, account);
            } catch (Exception e) {
                return "ERROR|FILTER_TRANSACTIONS_FAILED|" + e.getMessage();
            }
        }

        // --- REQ_REPORT: balas header FILE_SIZE ---
        // Format: REQ_REPORT|sessionToken
        if (line.startsWith("REQ_REPORT")) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                return "ERROR|SESSION_REQUIRED";
            }

            String sessionToken = parts[1];
            String username = validateAndGetUsername(sessionToken);

            if (username == null) {
                return "ERROR|SESSION_INVALID";
            }

            byte[] report = getReportBytes();
            int size = report.length;
            return "FILE_SIZE|" + size;
        }

        return "ERROR|UNKNOWN_COMMAND|" + line;
    }

    // ====== API BINARY UNTUK LAPORAN ======

    public byte[] getReportBytes() {
        List<Transaction> list = transactionService.getAllTransactions();
        return reportService.buildCsvBytes(list);
    }

    public void writeReportToStream(OutputStream out) throws IOException {
        byte[] data = getReportBytes();
        out.write(data);
        out.flush();
    }

    /**
     * Helper method to validate session token and return username
     * 
     * @param sessionToken The session token to validate
     * @return Username if valid, null if invalid or expired
     */
    private String validateAndGetUsername(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return null;
        }

        if (sessionManager.validateSession(sessionToken)) {
            sessionManager.updateLastAccessed(sessionToken);
            return sessionManager.getUsernameFromSession(sessionToken);
        }

        return null;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    // Expose username resolution for session token (used by ClientHandler for
    // SUBSCRIBE)
    public String getUsernameForSession(String sessionToken) {
        return validateAndGetUsername(sessionToken);
    }
}
