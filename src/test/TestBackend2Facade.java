import java.io.FileOutputStream;
import java.io.IOException;

public class TestBackend2Facade {

    // Helper kecil untuk ambil ID transaksi pertama dari respon DATA_ALL
    // Format: DATA_ALL|id;date;description;category;type;amount#id2;...
    private static String extractFirstTransactionId(String dataAllResponse) {
        if (dataAllResponse == null || !dataAllResponse.startsWith("DATA_ALL|")) {
            return null;
        }
        String payload = dataAllResponse.substring("DATA_ALL|".length());
        if (payload.isEmpty()) return null;

        String firstBlock;
        int hashIndex = payload.indexOf('#');
        if (hashIndex >= 0) {
            firstBlock = payload.substring(0, hashIndex);
        } else {
            firstBlock = payload;
        }

        int semicolonIndex = firstBlock.indexOf(';');
        if (semicolonIndex <= 0) {
            return null;
        }
        return firstBlock.substring(0, semicolonIndex);
    }

    public static void main(String[] args) {
        Backend2Facade backend = new Backend2Facade();

        System.out.println("=== TEST REGISTER ===");
        String reg1 = backend.handleTextCommand("REGISTER|indra|12345");
        String reg2 = backend.handleTextCommand("REGISTER|indra|12345");
        System.out.println("RESP REG 1: " + reg1);
        System.out.println("RESP REG 2: " + reg2);

        System.out.println("\n=== TEST LOGIN ===");
        String loginOk  = backend.handleTextCommand("LOGIN|indra|12345");
        String loginBad = backend.handleTextCommand("LOGIN|indra|salah");
        System.out.println("RESP LOGIN OK : " + loginOk);
        System.out.println("RESP LOGIN BAD: " + loginBad);

        System.out.println("\n=== TEST CHANGE PASSWORD ===");
        String changeOk  = backend.handleTextCommand("CHANGE_PASSWORD|indra|12345|99999");
        String changeBad = backend.handleTextCommand("CHANGE_PASSWORD|indra|12345|abcde");
        System.out.println("RESP CHANGE OK : " + changeOk);
        System.out.println("RESP CHANGE BAD: " + changeBad);

        System.out.println("\n=== TEST LOGIN SETELAH GANTI PASSWORD ===");
        String loginOld = backend.handleTextCommand("LOGIN|indra|12345");
        String loginNew = backend.handleTextCommand("LOGIN|indra|99999");
        System.out.println("RESP LOGIN OLD: " + loginOld);
        System.out.println("RESP LOGIN NEW: " + loginNew);

        System.out.println("\n=== TEST ADD (CREATE) & SUMMARY ===");
        String cmd1 = "ADD|indra|2025-12-02|Gaji Bulanan|Gaji|INCOME|8500000";
        String cmd2 = "ADD|indra|2025-12-03|Makan siang|Makanan|EXPENSE|25000";
        String cmd3 = "ADD|indra|2025-12-04|Ngopi|Hiburan|EXPENSE|15000";

        String respAdd1 = backend.handleTextCommand(cmd1);
        String respAdd2 = backend.handleTextCommand(cmd2);
        String respAdd3 = backend.handleTextCommand(cmd3);

        System.out.println("RESP ADD 1: " + respAdd1);
        System.out.println("RESP ADD 2: " + respAdd2);
        System.out.println("RESP ADD 3: " + respAdd3);

        System.out.println("\n=== TEST READ: GET_SUMMARY & GET_ALL ===");
        String summaryResp = backend.handleTextCommand("GET_SUMMARY");
        System.out.println("RESP SUMMARY: " + summaryResp);

        String allDataResp = backend.handleTextCommand("GET_ALL");
        System.out.println("RESP DATA_ALL: " + allDataResp);

        String firstId = extractFirstTransactionId(allDataResp);
        System.out.println("FIRST TRANSACTION ID (for update/delete): " + firstId);

        if (firstId != null) {
            System.out.println("\n=== TEST UPDATE TRANSACTION ===");
            String updateCmd = "UPDATE|" + firstId +
                    "|indra|2025-12-05|Gaji Bonus|Gaji|INCOME|5000000";
            String respUpdate = backend.handleTextCommand(updateCmd);
            System.out.println("RESP UPDATE: " + respUpdate);

            String allAfterUpdate = backend.handleTextCommand("GET_ALL");
            System.out.println("DATA_ALL AFTER UPDATE: " + allAfterUpdate);

            System.out.println("\n=== TEST DELETE TRANSACTION ===");
            String deleteCmd = "DELETE|" + firstId;
            String respDelete = backend.handleTextCommand(deleteCmd);
            System.out.println("RESP DELETE: " + respDelete);

            String allAfterDelete = backend.handleTextCommand("GET_ALL");
            System.out.println("DATA_ALL AFTER DELETE: " + allAfterDelete);
        } else {
            System.out.println("TIDAK ADA TRANSAKSI UNTUK DI-UPDATE / DELETE");
        }

        System.out.println("\n=== TEST REQ_REPORT & GENERATE CSV (STREAM) ===");
        String fileSizeHeader = backend.handleTextCommand("REQ_REPORT");
        System.out.println("RESP FILE_SIZE: " + fileSizeHeader);

        byte[] reportBytes = backend.getReportBytes();
        System.out.println("Report size (bytes): " + reportBytes.length);

        try (FileOutputStream fos = new FileOutputStream("reports/report_test.csv")) {
            fos.write(reportBytes);
            System.out.println("File reports/report_test.csv berhasil dibuat.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n=== SELESAI TEST CRUD BACKEND2FACADE ===");
    }
}
