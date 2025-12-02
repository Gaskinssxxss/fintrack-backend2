package model;

import java.io.Serializable;

public class FinancialSummary implements Serializable {

    private final long totalBalance;
    private final long totalIncome;
    private final long totalExpense;

    public FinancialSummary(long totalIncome, long totalExpense) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.totalBalance = totalIncome - totalExpense;
    }

    public long getTotalBalance() {
        return totalBalance;
    }

    public long getTotalIncome() {
        return totalIncome;
    }

    public long getTotalExpense() {
        return totalExpense;
    }

    /**
     * Dipakai untuk kirim ke client lewat socket.
     *
     * Format: SUMMARY|balance|income|expense
     */
    public String toProtocolString() {
        return "SUMMARY|" + totalBalance + "|" + totalIncome + "|" + totalExpense;
    }
}
