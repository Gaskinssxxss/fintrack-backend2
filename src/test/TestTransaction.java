public class TestTransaction {
    public static void main(String[] args) {
        Transaction t1 = new Transaction(
                "TXN-001",
                "indra",
                "2025-12-02",
                "Gaji Bulanan",
                "Gaji",
                "INCOME",
                8500000
        );

        String line = t1.toCSV();
        System.out.println("CSV: " + line);

        Transaction t2 = Transaction.fromCSV(line);
        System.out.println("Parsed: " + t2.getDescription() + " - " + t2.getAmount());
    }
}
