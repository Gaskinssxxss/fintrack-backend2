import java.util.List;

public class TestFileManager {
    public static void main(String[] args) {
        FileManager fm = new FileManager();

        Transaction t1 = new Transaction(
                "TXN-002",
                "indra",
                "2025-12-03",
                "Makan siang",
                "Makanan",
                "EXPENSE",
                25000
        );
        fm.saveTransaction(t1);
        System.out.println("Satu transaksi disimpan ke file.");

        List<Transaction> list = fm.loadTransactions();
        System.out.println("Isi file sekarang:");
        for (Transaction t : list) {
            System.out.println(t.getDate() + " - " + t.getDescription() + " - " + t.getAmount());
        }
    }
}
