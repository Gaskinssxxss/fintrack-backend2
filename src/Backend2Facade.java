import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Backend2Facade {

    private final TransactionService transactionService;
    private final ReportService reportService;
    private final UserManager userManager;

    public Backend2Facade() {
        this.transactionService = new TransactionService();
        this.reportService = new ReportService();
        this.userManager = new UserManager();
    }

    // ====== API TEXT-BASED UNTUK BACKEND 1 (via string command) ======

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
        if (line.startsWith("LOGIN|")) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) {
                return "ERROR|LOGIN_FORMAT";
            }
            String username = parts[1];
            String password = parts[2];

            boolean valid = userManager.validateLogin(username, password);
            if (valid) {
                return "LOGIN_OK|" + username;
            } else {
                return "LOGIN_FAIL|INVALID_CREDENTIALS";
            }
        }

        // --- CHANGE PASSWORD ---
        // Format: CHANGE_PASSWORD|username|oldPassword|newPassword
        if (line.startsWith("CHANGE_PASSWORD|")) {
            String[] parts = line.split("\\|");
            if (parts.length != 4) {
                return "ERROR|CHANGE_PASSWORD_FORMAT";
            }
            String username    = parts[1];
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
        // Format: ADD|username|date|description|category|type|amount
        if (line.startsWith("ADD|")) {
            transactionService.addTransactionFromCommand(line);
            FinancialSummary summary = transactionService.getSummary();
            return summary.toProtocolString(); // SUMMARY|...
        }

        // --- UPDATE transaksi ---
        // Format: UPDATE|id|username|date|description|category|type|amount
        if (line.startsWith("UPDATE|")) {
            boolean updated = transactionService.updateTransactionFromCommand(line);
            if (updated) {
                FinancialSummary summary = transactionService.getSummary();
                return summary.toProtocolString();
            } else {
                return "ERROR|UPDATE_NOT_FOUND";
            }
        }

        // --- DELETE transaksi ---
        // Format: DELETE|id
        if (line.startsWith("DELETE|")) {
            boolean deleted = transactionService.deleteTransactionFromCommand(line);
            if (deleted) {
                FinancialSummary summary = transactionService.getSummary();
                return summary.toProtocolString();
            } else {
                return "ERROR|DELETE_NOT_FOUND";
            }
        }

        // --- GET_ALL transaksi ---
        if ("GET_ALL".equalsIgnoreCase(line)) {
            return transactionService.buildAllTransactionsMessage();
        }

        // --- GET_SUMMARY untuk dashboard ---
        if ("GET_SUMMARY".equalsIgnoreCase(line)) {
            FinancialSummary summary = transactionService.getSummary();
            return summary.toProtocolString();
        }

        // --- REQ_REPORT: balas header FILE_SIZE ---
        if ("REQ_REPORT".equalsIgnoreCase(line)) {
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

    public TransactionService getTransactionService() {
        return transactionService;
    }
}
