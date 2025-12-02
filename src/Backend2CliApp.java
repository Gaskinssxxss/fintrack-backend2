import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import util.RequestLogger;

public class Backend2CliApp {

    private final Backend2Facade backend;
    private final Scanner scanner;
    private final RequestLogger logger;

    // "Session" sederhana di sisi CLI: siapa user yang sedang login
    private String currentUsername = null;

    // ANSI color codes for better visibility
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public Backend2CliApp() {
        this.backend = new Backend2Facade();
        this.scanner = new Scanner(System.in);
        this.logger = RequestLogger.getInstance();
    }

    // Helper method to execute command with logging
    private String executeCommand(String command) {
        // Log request
        logger.logRequest(command, currentUsername != null ? currentUsername : "GUEST");
        
        // Execute command
        String response = backend.handleTextCommand(command);
        
        // Log response
        if (response.startsWith("ERROR")) {
            logger.logError(command, response);
        } else {
            logger.logSuccess(command, response);
        }
        
        return response;
    }

    // Helper method to print status with color coding
    private void printStatus(String status, String message) {
        String color;
        String statusLabel;
        
        switch (status) {
            case "SUCCESS":
                color = GREEN;
                statusLabel = "[✓ SUCCESS]";
                break;
            case "ERROR":
                color = RED;
                statusLabel = "[✗ ERROR]";
                break;
            case "INFO":
                color = CYAN;
                statusLabel = "[ℹ INFO]";
                break;
            case "WARNING":
                color = YELLOW;
                statusLabel = "[⚠ WARNING]";
                break;
            default:
                color = RESET;
                statusLabel = "[" + status + "]";
        }
        
        System.out.println(color + BOLD + statusLabel + RESET + " " + message);
    }

    // Helper to print request/response with formatting
    private void printRequest(String command) {
        System.out.println(BLUE + "┌─ REQUEST" + RESET);
        System.out.println(BLUE + "│ " + RESET + command);
        System.out.println(BLUE + "└─" + RESET);
    }

    private void printResponse(String response, String statusCode) {
        System.out.println(CYAN + "┌─ RESPONSE [" + statusCode + "]" + RESET);
        System.out.println(CYAN + "│ " + RESET + response);
        System.out.println(CYAN + "└─" + RESET);
    }

    // Parse status code from response
    private String getStatusCode(String response) {
        if (response == null || response.isEmpty()) {
            return "UNKNOWN";
        }
        
        if (response.startsWith("OK")) return "200";
        if (response.startsWith("LOGIN_OK")) return "200";
        if (response.startsWith("SUMMARY")) return "200";
        if (response.startsWith("DATA_ALL")) return "200";
        if (response.startsWith("FILE_SIZE")) return "200";
        if (response.startsWith("ERROR")) return "400";
        
        return "UNKNOWN";
    }

    // Determine if response is success
    private boolean isSuccess(String response) {
        return response != null && 
               (response.startsWith("OK") || 
                response.startsWith("LOGIN_OK") || 
                response.startsWith("SUMMARY") || 
                response.startsWith("DATA_ALL") ||
                response.startsWith("FILE_SIZE"));
    }

    public static void main(String[] args) {
        Backend2CliApp app = new Backend2CliApp();
        app.run();
    }

    private void run() {
        System.out.println("===================================");
        System.out.println("  FinTrack Backend2 CLI Tester");
        System.out.println("===================================");
        System.out.println("Tips:");
        System.out.println("- Mulai dari menu User API untuk REGISTER & LOGIN.");
        System.out.println("- Setelah LOGIN, semua transaksi akan otomatis pakai username tersebut.");
        System.out.println("- Lalu coba Transaction API untuk ADD / UPDATE / DELETE.");
        System.out.println("- Lalu gunakan Report API untuk generate CSV.\n");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== MAIN MENU ===");
            System.out.println("User aktif : " + (currentUsername == null ? "(belum login)" : currentUsername));
            System.out.println("1. User API (Register / Login / Change Password)");
            System.out.println("2. Transaction API (CRUD & Summary)");
            System.out.println("3. Report API (REQ_REPORT & Generate CSV)");
            System.out.println("4. Keluar");
            System.out.print("Pilih menu [1-4]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    userMenu();
                    break;
                case "2":
                    transactionMenu();
                    break;
                case "3":
                    reportMenu();
                    break;
                case "4":
                    running = false;
                    System.out.println("Keluar dari CLI Tester. Bye!");
                    break;
                default:
                    System.out.println("Pilihan tidak dikenal. Coba lagi.");
            }
        }
    }

    // ===================== HELPER INPUT =====================

    private String askNonEmpty(String label, String exampleHint) {
        while (true) {
            if (exampleHint != null && !exampleHint.isEmpty()) {
                System.out.print(label + " (contoh: " + exampleHint + "): ");
            } else {
                System.out.print(label + ": ");
            }
            String val = scanner.nextLine().trim();
            if (!val.isEmpty()) {
                return val;
            }
            System.out.println("Input tidak boleh kosong. Coba lagi.");
        }
    }

    private double askDouble(String label, String exampleHint) {
        while (true) {
            if (exampleHint != null && !exampleHint.isEmpty()) {
                System.out.print(label + " (contoh: " + exampleHint + "): ");
            } else {
                System.out.print(label + ": ");
            }
            String txt = scanner.nextLine().trim();
            try {
                return Double.parseDouble(txt);
            } catch (NumberFormatException e) {
                System.out.println("Format angka tidak valid. Coba lagi.");
            }
        }
    }

    private String askTypeOption() {
        while (true) {
            System.out.println("Pilih Type Transaksi:");
            System.out.println("1. INCOME  (Pemasukan)");
            System.out.println("2. EXPENSE (Pengeluaran)");
            System.out.print("Pilih [1-2]: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    return "INCOME";
                case "2":
                    return "EXPENSE";
                default:
                    System.out.println("Pilihan tidak valid. Coba lagi.");
            }
        }
    }

    // ===================== USER MENU =====================

    private void userMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== USER API MENU ===");
            System.out.println("User aktif : " + (currentUsername == null ? "(belum login)" : currentUsername));
            System.out.println("1. REGISTER");
            System.out.println("2. LOGIN");
            System.out.println("3. CHANGE_PASSWORD");
            System.out.println("4. Kembali ke MAIN MENU");
            System.out.print("Pilih [1-4]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    doRegister();
                    break;
                case "2":
                    doLogin();
                    break;
                case "3":
                    doChangePassword();
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Pilihan tidak dikenal.");
            }
        }
    }

    private void doRegister() {
        System.out.println("\n" + BOLD + "--- REGISTER USER ---" + RESET);
        String username = askNonEmpty("Username", "indra");
        String password = askNonEmpty("Password", "12345");

        String cmd = "REGISTER|" + username + "|" + password;
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "User '" + username + "' berhasil didaftarkan");
        } else {
            printStatus("ERROR", "Registrasi gagal");
        }
    }

    private void doLogin() {
        System.out.println("\n" + BOLD + "--- LOGIN USER ---" + RESET);
        String username = askNonEmpty("Username", "indra");
        String password = askNonEmpty("Password", "12345");

        String cmd = "LOGIN|" + username + "|" + password;
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);

        if (resp.startsWith("LOGIN_OK|")) {
            // Ambil username dari response dan set sebagai user aktif
            String[] parts = resp.split("\\|");
            if (parts.length >= 2) {
                currentUsername = parts[1];
                printStatus("SUCCESS", "Login berhasil. User aktif: " + currentUsername);
            }
        } else {
            printStatus("ERROR", "Login gagal. User aktif tidak berubah");
        }
    }

    private void doChangePassword() {
        System.out.println("\n" + BOLD + "--- CHANGE PASSWORD ---" + RESET);

        if (currentUsername == null) {
            printStatus("WARNING", "Anda belum LOGIN. Silakan login dahulu di menu User API");
            return;
        }

        printStatus("INFO", "User yang akan diganti password-nya: " + currentUsername);
        String oldPass  = askNonEmpty("Password lama", "12345");
        String newPass  = askNonEmpty("Password baru", "99999");

        String cmd = "CHANGE_PASSWORD|" + currentUsername + "|" + oldPass + "|" + newPass;
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "Password berhasil diubah");
        } else {
            printStatus("ERROR", "Gagal mengubah password");
        }
    }

    // ===================== TRANSACTION MENU =====================

    private void transactionMenu() {
        if (currentUsername == null) {
            System.out.println();
            System.out.println("[WARNING] Anda belum LOGIN.");
            System.out.println("Silakan login dulu di menu User API sebelum mengelola transaksi.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== TRANSACTION API MENU ===");
            System.out.println("User aktif : " + currentUsername);
            System.out.println("1. ADD (Create)");
            System.out.println("2. UPDATE (Update by ID)");
            System.out.println("3. DELETE (Delete by ID)");
            System.out.println("4. GET_ALL (List semua transaksi)");
            System.out.println("5. GET_SUMMARY (Ringkasan total)");
            System.out.println("6. Kembali ke MAIN MENU");
            System.out.print("Pilih [1-6]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    doAddTransaction();
                    break;
                case "2":
                    doUpdateTransaction();
                    break;
                case "3":
                    doDeleteTransaction();
                    break;
                case "4":
                    doGetAllTransactions();
                    break;
                case "5":
                    doGetSummary();
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println("Pilihan tidak dikenal.");
            }
        }
    }

    private void doAddTransaction() {
        System.out.println("\n" + BOLD + "--- ADD TRANSACTION ---" + RESET);

        if (currentUsername == null) {
            printStatus("WARNING", "Anda belum LOGIN. Operasi dibatalkan");
            return;
        }

        String date        = askNonEmpty("Tanggal (YYYY-MM-DD)", "2025-12-02");
        String desc        = askNonEmpty("Deskripsi", "Gaji Bulanan");
        String category    = askNonEmpty("Kategori", "Gaji / Makanan / Transport");
        String type        = askTypeOption();
        double amount      = askDouble("Jumlah (angka)", "8500000");
        String accountName = askNonEmpty("Nama Akun", "BCA Savings / GoPay / Cash");
        String accountType = askNonEmpty("Tipe Akun", "Bank / Dompet Digital / Tunai");

        // Username tidak ditanya lagi, pakai currentUsername
        String cmd = "ADD|" + currentUsername + "|" + date + "|" + desc + "|" + category + "|" + type + "|" + amount + "|" + accountName + "|" + accountType;
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "Transaksi berhasil ditambahkan");
            printStatus("INFO", "Summary updated: " + resp);
        } else {
            printStatus("ERROR", "Gagal menambahkan transaksi");
        }
    }

    private void doUpdateTransaction() {
        System.out.println("\n" + BOLD + "--- UPDATE TRANSACTION ---" + RESET);

        if (currentUsername == null) {
            printStatus("WARNING", "Anda belum LOGIN. Operasi dibatalkan");
            return;
        }

        printStatus("INFO", "Tips: Jalankan GET_ALL untuk melihat ID transaksi");
        String id          = askNonEmpty("ID transaksi yang akan di-update", "622b3592-...");

        String date        = askNonEmpty("Tanggal baru (YYYY-MM-DD)", "2025-12-05");
        String desc        = askNonEmpty("Deskripsi baru", "Gaji Bonus");
        String category    = askNonEmpty("Kategori baru", "Gaji / Makanan / Hiburan");
        String type        = askTypeOption();
        double amount      = askDouble("Jumlah baru (angka)", "5000000");
        String accountName = askNonEmpty("Nama Akun baru", "BCA Savings / GoPay / Cash");
        String accountType = askNonEmpty("Tipe Akun baru", "Bank / Dompet Digital / Tunai");

        // Username tidak diminta lagi, pakai currentUsername
        String cmd = "UPDATE|" + id + "|" + currentUsername + "|" + date + "|" + desc + "|" + category + "|" + type + "|" + amount + "|" + accountName + "|" + accountType;
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "Transaksi berhasil diupdate");
        } else {
            printStatus("ERROR", "Gagal mengupdate transaksi");
        }
    }

    private void doDeleteTransaction() {
        System.out.println("\n" + BOLD + "--- DELETE TRANSACTION ---" + RESET);

        if (currentUsername == null) {
            printStatus("WARNING", "Anda belum LOGIN. Operasi dibatalkan");
            return;
        }

        printStatus("INFO", "Tips: Jalankan GET_ALL untuk melihat ID transaksi");
        String id = askNonEmpty("ID transaksi yang akan dihapus", "622b3592-...");

        String cmd = "DELETE|" + id;
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "Transaksi berhasil dihapus");
        } else {
            printStatus("ERROR", "Gagal menghapus transaksi");
        }
    }

    private void doGetAllTransactions() {
        System.out.println("\n" + BOLD + "--- GET_ALL TRANSACTIONS ---" + RESET);
        
        String cmd = "GET_ALL";
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "Data transaksi berhasil diambil");
            printStatus("INFO", "Format: DATA_ALL|id;date;description;category;type;amount#id2;...");
            printStatus("INFO", "ID digunakan untuk UPDATE / DELETE");
        } else {
            printStatus("ERROR", "Gagal mengambil data transaksi");
        }
    }

    private void doGetSummary() {
        System.out.println("\n" + BOLD + "--- GET_SUMMARY ---" + RESET);
        
        String cmd = "GET_SUMMARY";
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "Summary berhasil diambil");
            printStatus("INFO", "Format: SUMMARY|totalBalance|totalIncome|totalExpense");
        } else {
            printStatus("ERROR", "Gagal mengambil summary");
        }
    }

    // ===================== REPORT MENU =====================

    private void reportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== REPORT API MENU ===");
            System.out.println("User aktif : " + (currentUsername == null ? "(belum login)" : currentUsername));
            System.out.println("1. REQ_REPORT (tampilkan FILE_SIZE)");
            System.out.println("2. Generate & Simpan CSV (report_cli.csv atau custom)");
            System.out.println("3. Kembali ke MAIN MENU");
            System.out.print("Pilih [1-3]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    doReqReportHeader();
                    break;
                case "2":
                    doGenerateReportFile();
                    break;
                case "3":
                    back = true;
                    break;
                default:
                    System.out.println("Pilihan tidak dikenal.");
            }
        }
    }

    private void doReqReportHeader() {
        System.out.println("\n" + BOLD + "--- REQ_REPORT HEADER ---" + RESET);
        
        String cmd = "REQ_REPORT";
        printRequest(cmd);
        
        String resp = executeCommand(cmd);
        String statusCode = getStatusCode(resp);
        printResponse(resp, statusCode);
        
        if (isSuccess(resp)) {
            printStatus("SUCCESS", "Report header berhasil diambil");
            printStatus("INFO", "Format: FILE_SIZE|<panjang_dalam_byte>");
        } else {
            printStatus("ERROR", "Gagal mengambil report header");
        }
    }

    private void doGenerateReportFile() {
        System.out.println("\n" + BOLD + "--- GENERATE REPORT CSV ---" + RESET);
        printStatus("INFO", "File akan disimpan di folder reports/ (atau path yang kamu isi)");
        String filename = askNonEmpty("Nama file output", "reports/report_cli.csv");

        printStatus("INFO", "Generating report bytes...");
        byte[] data = backend.getReportBytes();
        
        printStatus("INFO", "Writing to file: " + filename);
        printStatus("INFO", "File size: " + data.length + " bytes");
        
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(data);
            printStatus("SUCCESS", "Report berhasil disimpan ke: " + filename);
            printStatus("INFO", "Total bytes written: " + data.length);
        } catch (IOException e) {
            printStatus("ERROR", "Gagal menulis file: " + e.getMessage());
        }
    }
}
