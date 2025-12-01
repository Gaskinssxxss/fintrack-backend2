import java.io.Serializable;

public class FinancialSummary implements Serializable {

    private double totalBalance;
    private double totalIncome;
    private double totalExpense;

    public FinancialSummary(double totalIncome, double totalExpense) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.totalBalance = totalIncome - totalExpense;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    /**
     * Dipakai untuk kirim ke client lewat socket.
     * Angka dibulatkan ke long supaya tidak keluar dalam format 1.7E7.
     *
     * Format: SUMMARY|balance|income|expense
     */
    public String toProtocolString() {
        long balance = Math.round(totalBalance);
        long income  = Math.round(totalIncome);
        long expense = Math.round(totalExpense);

        return "SUMMARY|" + balance + "|" + income + "|" + expense;
    }
}
