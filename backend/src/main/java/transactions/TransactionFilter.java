package transactions;

import authorization.User;
import database.Bank;
import database.Category;
import database.DataLoader;
import database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

public class TransactionFilter {
    public final String greater_identificator = "-gt";
    public final String less_identificator = "-lw";
    public final String num_identificator = "-num";

    public List<Transaction> getUserTransactions(User user, Map<String, List<String>> filters) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");
        List<Object> parameters = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        parameters.add(user.getId());

        if (!filters.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue().getFirst();

                String operator = "=";
                boolean toInt = false;

                if (key.contains(greater_identificator)) {
                    operator = ">=";
                    key = key.replace(greater_identificator, "");
                    toInt = true;
                } else if (key.contains(less_identificator)) {
                    operator = "<=";
                    key = key.replace(less_identificator, "");
                    toInt = true;
                } else if (key.contains(num_identificator)) {
                    key = key.replace(num_identificator, "");
                }

                if (key.contains("transaction_date") || key.contains("created_at")) parameters.add(LocalDateTime.parse(val));
                else if (toInt) parameters.add(Integer.parseInt(val));
                else parameters.add(val);

                query.append(" AND ").append(key).append(" ").append(operator).append(" ?");
            }
        }

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(query.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i)); // Bind values safely
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        }
    }

    public LinkedHashMap<String, Object> getTransactionsByTime(LocalDateTime startingTimestamp, List<Transaction> originalTransactions) {
        LocalDate firstDate = startingTimestamp.toLocalDate();
        List<Transaction> transactions = new ArrayList<>(originalTransactions);
        // 1) sort chronologically
        transactions.sort(Comparator.comparing(Transaction::getTransactionDate));

        // WEEKLY
        List<String> weeks = new ArrayList<>();
        List<String> weeklyCounts = new ArrayList<>();
        List<String> weeklyIncomeCounts = new ArrayList<>();
        List<String> weeklyExpenseCounts = new ArrayList<>();
        List<String> weeklyIncomeAmounts = new ArrayList<>();
        List<String> weeklyExpenseAmounts = new ArrayList<>();
        // MONTHLY
        List<String> months = new ArrayList<>();
        List<String> monthlyCounts = new ArrayList<>();
        List<String> monthlyIncomeCounts = new ArrayList<>();
        List<String> monthlyExpenseCounts = new ArrayList<>();
        List<String> monthlyIncomeAmounts = new ArrayList<>();
        List<String> monthlyExpenseAmounts = new ArrayList<>();
        // QUARTERLY
        List<String> quarters = new ArrayList<>();
        List<String> quarterlyCounts = new ArrayList<>();
        List<String> quarterlyIncomeCounts = new ArrayList<>();
        List<String> quarterlyExpenseCounts = new ArrayList<>();
        List<String> quarterlyIncomeAmounts = new ArrayList<>();
        List<String> quarterlyExpenseAmounts = new ArrayList<>();
        // YEARLY
        List<String> years = new ArrayList<>();
        List<String> yearlyCounts = new ArrayList<>();
        List<String> yearlyIncomeCounts = new ArrayList<>();
        List<String> yearlyExpenseCounts = new ArrayList<>();
        List<String> yearlyIncomeAmounts = new ArrayList<>();
        List<String> yearlyExpenseAmounts = new ArrayList<>();

        Map<String, Integer> bankSenders = new HashMap<>();
        Map<String, Integer> bankRecipients = new HashMap<>();

        Map<String, Integer> incomeCategoriesAmount = new HashMap<>();
        Map<String, Integer> expenseCategoriesAmount = new HashMap<>();

        // WEEKLY: Monday of ISO week
        LocalDate weekStart = firstDate.with(WeekFields.ISO.dayOfWeek(), 1);

        // MONTHLY: first day of month
        LocalDate monthStart = firstDate.withDayOfMonth(1);

        // QUARTERLY: round down to quarter start
        int monthValue = firstDate.getMonthValue();
        int quarterStartMonth = ((monthValue - 1) / 3) * 3 + 1;  // 1, 4, 7, or 10
        LocalDate quarterStart = LocalDate.of(firstDate.getYear(), quarterStartMonth, 1);

        // YEARLY: first day of year
        LocalDate yearStart    = LocalDate.of(firstDate.getYear(), 1, 1);

        int weekCount = 0;
        int weekIncomeCount = 0;
        int weekExpenseCount = 0;
        int weekIncomeAmount = 0;
        int weekExpenseAmount = 0;
        int monthCount = 0;
        int monthIncomeCount = 0;
        int monthExpenseCount = 0;
        int monthIncomeAmount = 0;
        int monthExpenseAmount = 0;
        int quarterCount = 0;
        int quarterIncomeCount = 0;
        int quarterExpenseCount = 0;
        int quarterIncomeAmount = 0;
        int quarterExpenseAmount = 0;
        int yearCount = 0;
        int yearIncomeCount = 0;
        int yearExpenseCount = 0;
        int yearIncomeAmount = 0;
        int yearExpenseAmount = 0;

        int completedTransactions = 0;
        int cancelledTransactions = 0;

        for (Transaction tx : transactions) {
            LocalDate txDate = tx.getTransactionDate().toLocalDate();

            // compute target bucket starts
            LocalDate txWeekStart = txDate.with(WeekFields.ISO.dayOfWeek(), 1);
            LocalDate txMonthStart = txDate.withDayOfMonth(1);
            // round down to quarter start for this tx
            int txQuarterStartMonth = ((txDate.getMonthValue() - 1) / 3) * 3 + 1;
            LocalDate txQuarterStart = LocalDate.of(txDate.getYear(), txQuarterStartMonth, 1);
            LocalDate txYearStart = LocalDate.of(txDate.getYear(), 1, 1);

            int type = tx.getType().getTypeID();

            if (tx.getStatus().getStatusID() == 0) cancelledTransactions++;
            else if (tx.getStatus().getFinal()) completedTransactions++;

            Bank bankSender = tx.getSenderBank();
            if (!bankSenders.containsKey(bankSender.getName()))
                bankSenders.put(bankSender.getName(), 0);
            bankSenders.replace(bankSender.getName(), bankSenders.get(bankSender.getName()) + tx.getAmount());

            Bank bankRecipient = tx.getRecipientBank();
            if (!bankRecipients.containsKey(bankRecipient.getName()))
                bankRecipients.put(bankRecipient.getName(), 0);
            bankRecipients.replace(bankRecipient.getName(), bankRecipients.get(bankRecipient.getName()) + tx.getAmount());

            // — flush weekly buckets —
            while (txWeekStart.isAfter(weekStart)) {
                addTransactionToOutput(weeks, weekStart, weeklyCounts, weekCount, "week");
                weeklyIncomeCounts.add(String.valueOf(weekIncomeCount));
                weeklyExpenseCounts.add(String.valueOf(weekExpenseCount));
                weeklyIncomeAmounts.add(String.valueOf(weekIncomeAmount));
                weeklyExpenseAmounts.add(String.valueOf(weekExpenseAmount));
                weekStart = weekStart.plusWeeks(1);
                weekCount = 0;
                weekIncomeCount = 0;
                weekExpenseCount = 0;
                weekIncomeAmount = 0;
                weekExpenseAmount = 0;
            }

            // — flush monthly buckets —
            while (txMonthStart.isAfter(monthStart)) {
                addTransactionToOutput(months, monthStart, monthlyCounts, monthCount, "month");
                monthlyIncomeCounts.add(String.valueOf(monthIncomeCount));
                monthlyExpenseCounts.add(String.valueOf(monthExpenseCount));
                monthlyIncomeAmounts.add(String.valueOf(monthIncomeAmount));
                monthlyExpenseAmounts.add(String.valueOf(monthExpenseAmount));
                monthStart = monthStart.plusMonths(1);
                monthCount = 0;
                monthIncomeCount = 0;
                monthExpenseCount = 0;
                monthIncomeAmount = 0;
                monthExpenseAmount = 0;
            }

            // — flush quarterly buckets —
            while (txQuarterStart.isAfter(quarterStart)) {
                // label this quarter as "YYYY-MM-DD to YYYY-MM-DD"
                LocalDate qEnd = quarterStart.plusMonths(3).minusDays(1);
                addTransactionToOutput(quarters, quarterStart, quarterlyCounts, quarterCount, "quarter");
                quarterlyIncomeCounts.add(String.valueOf(quarterIncomeCount));
                quarterlyExpenseCounts.add(String.valueOf(quarterExpenseCount));
                quarterlyIncomeAmounts.add(String.valueOf(quarterIncomeAmount));
                quarterlyExpenseAmounts.add(String.valueOf(quarterExpenseAmount));

                // advance quarterStart by 3 months
                quarterStart = quarterStart.plusMonths(3);
                quarterCount = 0;
                quarterIncomeCount = 0;
                quarterExpenseCount = 0;
                quarterIncomeAmount = 0;
                quarterExpenseAmount = 0;
            }

            // — flush yearly buckets —
            while (txYearStart.isAfter(yearStart)) {
                addTransactionToOutput(years, yearStart, yearlyCounts, yearCount, "year");
                yearlyIncomeCounts.add(String.valueOf(yearIncomeCount));
                yearlyExpenseCounts.add(String.valueOf(yearExpenseCount));
                yearlyIncomeAmounts.add(String.valueOf(yearIncomeAmount));
                yearlyExpenseAmounts.add(String.valueOf(yearExpenseAmount));
                yearStart = yearStart.plusYears(1);
                yearCount = 0;
                yearIncomeCount = 0;
                yearExpenseCount = 0;
                yearIncomeAmount = 0;
                yearExpenseAmount = 0;
            }

            // count into all current buckets
            weekCount++;
            monthCount++;
            quarterCount++;
            yearCount++;

            if (type == 1) {
                weekIncomeCount++;
                weekIncomeAmount += tx.getAmount();
                monthIncomeCount++;
                monthIncomeAmount += tx.getAmount();
                quarterIncomeCount++;
                quarterIncomeAmount += tx.getAmount();
                yearIncomeCount++;
                yearIncomeAmount += tx.getAmount();

                String categoryName = tx.getCategory().getName();
                if (!incomeCategoriesAmount.containsKey(categoryName)) incomeCategoriesAmount.put(categoryName, 0);
                incomeCategoriesAmount.replace(categoryName, incomeCategoriesAmount.get(categoryName) + tx.getAmount());
            }
            else {
                weekExpenseCount++;
                weekExpenseAmount += tx.getAmount();
                monthExpenseCount++;
                monthExpenseAmount += tx.getAmount();
                quarterExpenseCount++;
                quarterExpenseAmount += tx.getAmount();
                yearExpenseCount++;
                yearExpenseAmount += tx.getAmount();

                String categoryName = tx.getCategory().getName();
                if (!expenseCategoriesAmount.containsKey(categoryName)) expenseCategoriesAmount.put(categoryName, 0);
                expenseCategoriesAmount.replace(categoryName, expenseCategoriesAmount.get(categoryName) + tx.getAmount());
            }
        }

        // week
        addTransactionToOutput(weeks, weekStart, weeklyCounts, weekCount, "week");
        weeklyIncomeCounts.add(String.valueOf(weekIncomeCount));
        weeklyExpenseCounts.add(String.valueOf(weekExpenseCount));
        weeklyIncomeAmounts.add(String.valueOf(weekIncomeAmount));
        weeklyExpenseAmounts.add(String.valueOf(weekExpenseAmount));

        // month
        addTransactionToOutput(months, monthStart, monthlyCounts, monthCount, "month");
        monthlyIncomeCounts.add(String.valueOf(monthIncomeCount));
        monthlyExpenseCounts.add(String.valueOf(monthExpenseCount));
        monthlyIncomeAmounts.add(String.valueOf(monthIncomeAmount));
        monthlyExpenseAmounts.add(String.valueOf(monthExpenseAmount));

        // quarter
        addTransactionToOutput(quarters, quarterStart, quarterlyCounts, quarterCount, "quarter");
        quarterlyIncomeCounts.add(String.valueOf(quarterIncomeCount));
        quarterlyExpenseCounts.add(String.valueOf(quarterExpenseCount));
        quarterlyIncomeAmounts.add(String.valueOf(quarterIncomeAmount));
        quarterlyExpenseAmounts.add(String.valueOf(quarterExpenseAmount));

        // year
        addTransactionToOutput(years, yearStart, yearlyCounts, yearCount, "year");
        yearlyIncomeCounts.add(String.valueOf(yearIncomeCount));
        yearlyExpenseCounts.add(String.valueOf(yearExpenseCount));
        yearlyIncomeAmounts.add(String.valueOf(yearIncomeAmount));
        yearlyExpenseAmounts.add(String.valueOf(yearExpenseAmount));


        LinkedHashMap<String, List<List<String>>> firstDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, List<List<String>>> secondDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, List<List<String>>> thirdDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> fourthDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, Map<String, Integer>> fifthDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, Map<String, Integer>> sixthDashboard = new LinkedHashMap<>();
        firstDashboard.put("weekly", Arrays.asList(weeks, weeklyCounts));
        firstDashboard.put("monthly", Arrays.asList(months, monthlyCounts));
        firstDashboard.put("quarterly", Arrays.asList(quarters, quarterlyCounts));
        firstDashboard.put("yearly", Arrays.asList(years, yearlyCounts));
        secondDashboard.put("weekly", Arrays.asList(weeks, weeklyIncomeCounts, weeklyExpenseCounts));
        secondDashboard.put("monthly", Arrays.asList(months, monthlyIncomeCounts, monthlyExpenseCounts));
        secondDashboard.put("quarterly", Arrays.asList(quarters, quarterlyIncomeCounts, quarterlyExpenseCounts));
        secondDashboard.put("yearly", Arrays.asList(years, yearlyIncomeCounts, yearlyExpenseCounts));
        thirdDashboard.put("weekly", Arrays.asList(weeks, weeklyIncomeAmounts, weeklyExpenseAmounts));
        thirdDashboard.put("monthly", Arrays.asList(months, monthlyIncomeAmounts, monthlyExpenseAmounts));
        thirdDashboard.put("quarterly", Arrays.asList(quarters, quarterlyIncomeAmounts, quarterlyExpenseAmounts));
        thirdDashboard.put("yearly", Arrays.asList(years, yearlyIncomeAmounts, yearlyExpenseAmounts));
        fourthDashboard.put("Completed Transactions:", completedTransactions);
        fourthDashboard.put("Cancelled Transactions:", cancelledTransactions);
        fifthDashboard.put("BankSenders:", bankSenders);
        fifthDashboard.put("BankReceivers:", bankRecipients);
        sixthDashboard.put("Income Categories:", incomeCategoriesAmount);
        sixthDashboard.put("Expenses Categories:", expenseCategoriesAmount);
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("Dashboard 1:", firstDashboard);
        result.put("Dashboard 2:", secondDashboard);
        result.put("Dashboard 3:", thirdDashboard);
        result.put("Dashboard 4", fourthDashboard);
        result.put("Dashboard 5:", fifthDashboard);
        result.put("Dashboard 6:", sixthDashboard);
        return result;
    }

    private void addTransactionToOutput(List<String> labels, LocalDate date, List<String> values, int value, String type) {
        DateTimeFormatter formatter;
        switch (type) {
            case "week": {
                formatter = DateTimeFormatter.ofPattern("MM-dd", new Locale("ru"));
                labels.add("неделя " + date.format(formatter));
                values.add(String.valueOf(value));
                break;
            }
            case "month": {
                formatter = DateTimeFormatter.ofPattern("MMM", new Locale("ru"));
                labels.add(date.format(formatter));
                values.add(String.valueOf(value));
                break;
            }
            case "quarter": {
                int quarter = (date.getMonthValue() - 1) / 3 + 1;
                labels.add("Q" + quarter + " " + date.getYear());
                values.add(String.valueOf(value));
                break;
            }
            case "year": {
                formatter = DateTimeFormatter.ofPattern("yyyy", new Locale("ru"));
                labels.add(date.format(formatter));
                values.add(String.valueOf(value));
                break;
            }
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionID(resultSet.getLong("transaction_id"));  // transaction_id

        transaction.setUserID(resultSet.getLong("user_id"));  // user_id

        transaction.setLegalType(DataLoader.getLegalTypeByID(resultSet.getInt("legal_type_id")));  // person_type_id (assuming this is legalTypeID)

        transaction.setTransactionDate(resultSet.getTimestamp("transaction_date").toLocalDateTime());  // transaction_date

        transaction.setType(DataLoader.getTransactionTypeByID(resultSet.getInt("type_id")));  // type_id

        transaction.setAmount(resultSet.getInt("amount"));  // amount (assuming it's stored as an integer)

        transaction.setStatus(DataLoader.getTransactionStatusByID(resultSet.getInt("status_id")));  // status_id

        transaction.setSenderBank(DataLoader.getBankByID(resultSet.getInt("sender_bank_id")));

        transaction.setRecipientBank(DataLoader.getBankByID(resultSet.getInt("recipient_bank_id"))); // bank (assuming sender_bank_id is the bank column)

        transaction.setAccountNumber(resultSet.getString("account_number"));  // account_number

        transaction.setRecipientNumber(resultSet.getString("recipient_number"));  // Recipient_number (assuming Reciiver_bank_id maps to recipient number)

        transaction.setRecipientTIN(resultSet.getLong("recipient_tin"));  // Recipient_tin (Reciiver_inn)

        transaction.setRecipientPhone(resultSet.getString("recipient_phone"));  // Reciiver_phone (phone)

        transaction.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());  // created_at

        transaction.setCategory(DataLoader.getCategoryByID(resultSet.getInt("category_id")));  // category_id

        transaction.setComment(resultSet.getString("comment"));  // comment


        return transaction;
    }
}
