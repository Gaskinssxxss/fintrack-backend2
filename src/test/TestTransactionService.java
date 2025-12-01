import java.util.List;

public class TestTransactionService {
    public static void main(String[] args) {
        TransactionService service = new TransactionService();

        service.addTransaction("indra", "2025-12-02",
                "Gaji Bulanan", "Gaji", "INCOME", 8500000);

        service.addTransaction("indra", "2025-12-03",
                "Makan siang", "Makanan", "EXPENSE", 25000);

        FinancialSummary summary = service.getSummary();
        System.out.println("Total Income   : " + summary.getTotalIncome());
        System.out.println("Total Expense  : " + summary.getTotalExpense());
        System.out.println("Total Balance  : " + summary.getTotalBalance());

        String msg = service.buildAllTransactionsMessage();
        System.out.println("DATA_ALL msg   : " + msg);

        FileManager fm = new FileManager();
        List<Transaction> list = fm.loadTransactions();
        System.out.println("Isi file saat ini:");
        for (Transaction t : list) {
            System.out.println(t.getDate() + " | " + t.getDescription() + " | " + t.getAmount());
        }
    }
}
