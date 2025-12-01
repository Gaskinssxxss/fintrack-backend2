import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Backend2CliApp {

    private final Backend2Facade backend;
    private final Scanner scanner;

    // "Session" sederhana di sisi CLI: siapa user yang sedang login
    private String currentUsername = null;

    public Backend2CliApp() {
        this.backend = new Backend2Facade();
        this.scanner = new Scanner(System.in);
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
        System.out.println("\n--- REGISTER USER ---");
        String username = askNonEmpty("Username", "indra");
        String password = askNonEmpty("Password", "12345");

        String cmd = "REGISTER|" + username + "|" + password;
        String resp = backend.handleTextCommand(cmd);
        System.out.println("RESP: " + resp);
    }

    private void doLogin() {
        System.out.println("\n--- LOGIN USER ---");
        String username = askNonEmpty("Username", "indra");
        String password = askNonEmpty("Password", "12345");

        String cmd = "LOGIN|" + username + "|" + password;
        String resp = backend.handleTextCommand(cmd);
        System.out.println("RESP: " + resp);

        if (resp.startsWith("LOGIN_OK|")) {
            // Ambil username dari response dan set sebagai user aktif
            String[] parts = resp.split("\\|");
            if (parts.length >= 2) {
                currentUsername = parts[1];
                System.out.println(">> Login berhasil. User aktif sekarang: " + currentUsername);
            }
        } else {
            System.out.println(">> Login gagal. User aktif tidak berubah.");
        }
    }

    private void doChangePassword() {
        System.out.println("\n--- CHANGE PASSWORD ---");

        if (currentUsername == null) {
            System.out.println("Anda belum LOGIN. Silakan login dahulu di menu User API.");
            return;
        }

        System.out.println("User yang akan diganti password-nya: " + currentUsername);
        String oldPass  = askNonEmpty("Password lama", "12345");
        String newPass  = askNonEmpty("Password baru", "99999");

        String cmd = "CHANGE_PASSWORD|" + currentUsername + "|" + oldPass + "|" + newPass;
        String resp = backend.handleTextCommand(cmd);
        System.out.println("RESP: " + resp);
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
        System.out.println("\n--- ADD TRANSACTION ---");

        if (currentUsername == null) {
            System.out.println("Anda belum LOGIN. Operasi dibatalkan.");
            return;
        }

        String date     = askNonEmpty("Tanggal (YYYY-MM-DD)", "2025-12-02");
        String desc     = askNonEmpty("Deskripsi", "Gaji Bulanan");
        String category = askNonEmpty("Kategori", "Gaji / Makanan / Transport");
        String type     = askTypeOption();
        double amount   = askDouble("Jumlah (angka)", "8500000");

        // Username tidak ditanya lagi, pakai currentUsername
        String cmd = "ADD|" + currentUsername + "|" + date + "|" + desc + "|" + category + "|" + type + "|" + amount;
        String resp = backend.handleTextCommand(cmd);
        System.out.println("RESP (SUMMARY): " + resp);
    }

    private void doUpdateTransaction() {
        System.out.println("\n--- UPDATE TRANSACTION ---");

        if (currentUsername == null) {
            System.out.println("Anda belum LOGIN. Operasi dibatalkan.");
            return;
        }

        System.out.println("Tips:");
        System.out.println("- Jalankan dulu menu: GET_ALL untuk melihat ID transaksi.");
        System.out.println("- Copy ID dari sana, lalu paste di sini.");
        String id       = askNonEmpty("ID transaksi yang akan di-update", "622b3592-...");

        String date     = askNonEmpty("Tanggal baru (YYYY-MM-DD)", "2025-12-05");
        String desc     = askNonEmpty("Deskripsi baru", "Gaji Bonus");
        String category = askNonEmpty("Kategori baru", "Gaji / Makanan / Hiburan");
        String type     = askTypeOption();
        double amount   = askDouble("Jumlah baru (angka)", "5000000");

        // Username tidak diminta lagi, pakai currentUsername
        String cmd = "UPDATE|" + id + "|" + currentUsername + "|" + date + "|" + desc + "|" + category + "|" + type + "|" + amount;
        String resp = backend.handleTextCommand(cmd);
        System.out.println("RESP: " + resp);
    }

    private void doDeleteTransaction() {
        System.out.println("\n--- DELETE TRANSACTION ---");

        if (currentUsername == null) {
            System.out.println("Anda belum LOGIN. Operasi dibatalkan.");
            return;
        }

        System.out.println("Tips:");
        System.out.println("- Jalankan dulu menu: GET_ALL untuk melihat ID transaksi.");
        System.out.println("- Copy ID dari sana, lalu paste di sini.");
        String id = askNonEmpty("ID transaksi yang akan dihapus", "622b3592-...");

        String cmd = "DELETE|" + id;
        String resp = backend.handleTextCommand(cmd);
        System.out.println("RESP: " + resp);
    }

    private void doGetAllTransactions() {
        System.out.println("\n--- GET_ALL TRANSACTIONS ---");
        String resp = backend.handleTextCommand("GET_ALL");
        System.out.println("RESP:");
        System.out.println(resp);
        System.out.println("\nCatatan:");
        System.out.println("- Format: DATA_ALL|id;date;description;category;type;amount#id2;...");
        System.out.println("- ID inilah yang dipakai untuk UPDATE / DELETE.");
    }

    private void doGetSummary() {
        System.out.println("\n--- GET_SUMMARY ---");
        String resp = backend.handleTextCommand("GET_SUMMARY");
        System.out.println("RESP: " + resp);
        System.out.println("Format: SUMMARY|totalBalance|totalIncome|totalExpense");
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
        System.out.println("\n--- REQ_REPORT HEADER ---");
        String resp = backend.handleTextCommand("REQ_REPORT");
        System.out.println("RESP: " + resp);
        System.out.println("Format: FILE_SIZE|<panjang_dalam_byte>");
    }

    private void doGenerateReportFile() {
        System.out.println("\n--- GENERATE REPORT CSV ---");
        System.out.println("File akan disimpan di folder reports/ (atau path yang kamu isi).");
        String filename = askNonEmpty("Nama file output", "reports/report_cli.csv");

        byte[] data = backend.getReportBytes();
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(data);
            System.out.println("Report berhasil disimpan ke file: " + filename);
            System.out.println("Ukuran (bytes): " + data.length);
        } catch (IOException e) {
            System.out.println("Gagal menulis file: " + e.getMessage());
        }
    }
}
