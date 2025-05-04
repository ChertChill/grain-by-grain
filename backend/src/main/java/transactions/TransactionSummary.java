package transactions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionSummary {
    private final int totalCount;
    private final int totalIncome;
    private final int totalExpense;
    private final int balance;

    public TransactionSummary(List<Transaction> transactions) {
        this.totalCount = transactions.size();

        int income = 0;
        int expense = 0;

        for (Transaction tx : transactions) {
            if (tx.getStatus().getStatusID() == 5) {
                if (tx.getType().getTypeID() == 1) { // 1 - income
                    income += tx.getAmount();
                } else { // other - expense
                    expense += tx.getAmount();
                }
            }
        }

        this.totalIncome = income;
        this.totalExpense = expense;
        this.balance = income - expense;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total_count", totalCount);
        summary.put("total_income", totalIncome);
        summary.put("total_expense", totalExpense);
        summary.put("balance", balance);
        return summary;
    }
}