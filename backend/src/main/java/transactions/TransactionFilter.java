package transactions;

import authorization.User;
import database.DataLoader;
import database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

public class TransactionFilter {
    private final String greater_identificator = "-gt";
    private final String less_identificator = "-lw";
    private final String num_identificator = "-num";

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

                if (key.contains(greater_identificator)) {
                    operator = ">=";
                    key = key.replace(greater_identificator, "");
                    parameters.add(Integer.parseInt(val));
                } else if (key.contains(less_identificator)) {
                    operator = "<=";
                    key = key.replace(less_identificator, "");
                    parameters.add(Integer.parseInt(val));
                } else if (key.contains(num_identificator)) {
                    key = key.replace(num_identificator, "");
                    parameters.add(Integer.parseInt(val));
                } else parameters.add(val);

                query.append(" AND ").append(key).append(" ").append(operator).append(" ?");
            }
        }

//        if (filters.containsKey("amount")) {
//            query.append(" AND amount = ?");
//            parameters.add(Integer.valueOf(filters.get("amount").getFirst()));
//        }

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

    public LinkedHashMap<String, LinkedHashMap<String, List<List<String>>>> getTransactionsByTime(LocalDate firstDate, List<Transaction> originalTransactions) {
        List<Transaction> transactions = new ArrayList<>(originalTransactions);
        // 1) sort chronologically
        transactions.sort(Comparator.comparing(Transaction::getTransactionDate));

        // WEEKLY
        List<String> weeks = new ArrayList<>();
        List<String> weeklyCounts = new ArrayList<>();
        // MONTHLY
        List<String> months = new ArrayList<>();
        List<String> monthlyCounts = new ArrayList<>();
        // QUARTERLY
        List<String> quarters = new ArrayList<>();
        List<String> quarterlyCounts = new ArrayList<>();
        // YEARLY
        List<String> years = new ArrayList<>();
        List<String> yearlyCounts = new ArrayList<>();

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
        int monthCount = 0;
        int quarterCount = 0;
        int yearCount = 0;

        for (Transaction tx : transactions) {
            LocalDate txDate = tx.getTransactionDate().toLocalDate();

            // compute target bucket starts
            LocalDate txWeekStart = txDate.with(WeekFields.ISO.dayOfWeek(), 1);
            LocalDate txMonthStart = txDate.withDayOfMonth(1);
            // round down to quarter start for this tx
            int txQuarterStartMonth = ((txDate.getMonthValue() - 1) / 3) * 3 + 1;
            LocalDate txQuarterStart = LocalDate.of(txDate.getYear(), txQuarterStartMonth, 1);
            LocalDate txYearStart = LocalDate.of(txDate.getYear(), 1, 1);

            // — flush weekly buckets —
            while (txWeekStart.isAfter(weekStart)) {
                addTransactionToOutput(weeks, weekStart, weeklyCounts, weekCount, "week");
                weekStart = weekStart.plusWeeks(1);
                weekCount = 0;
            }

            // — flush monthly buckets —
            while (txMonthStart.isAfter(monthStart)) {
                addTransactionToOutput(months, monthStart, monthlyCounts, monthCount, "month");
                monthStart = monthStart.plusMonths(1);
                monthCount = 0;
            }

            // — flush quarterly buckets —
            while (txQuarterStart.isAfter(quarterStart)) {
                // label this quarter as "YYYY-MM-DD to YYYY-MM-DD"
                LocalDate qEnd = quarterStart.plusMonths(3).minusDays(1);
                addTransactionToOutput(quarters, quarterStart, quarterlyCounts, quarterCount, "quarter");

                // advance quarterStart by 3 months
                quarterStart = quarterStart.plusMonths(3);
                quarterCount = 0;
            }

            // — flush yearly buckets —
            while (txYearStart.isAfter(yearStart)) {
                addTransactionToOutput(years, yearStart, yearlyCounts, yearCount, "year");
                yearStart = yearStart.plusYears(1);
                yearCount = 0;
            }

            // count into all current buckets
            weekCount++;
            monthCount++;
            quarterCount++;
            yearCount++;
        }

        // week
        addTransactionToOutput(weeks, weekStart, weeklyCounts, weekCount, "week");

        // month
        addTransactionToOutput(months, monthStart, monthlyCounts, monthCount, "month");

        // quarter
        addTransactionToOutput(quarters, quarterStart, quarterlyCounts, quarterCount, "quarter");

        // year
        addTransactionToOutput(years, yearStart, yearlyCounts, yearCount, "year");


        LinkedHashMap<String, List<List<String>>> firstDashboard = new LinkedHashMap<>();
        firstDashboard.put("weekly", Arrays.asList(weeks, weeklyCounts));
        firstDashboard.put("monthly", Arrays.asList(months, monthlyCounts));
        firstDashboard.put("quarterly", Arrays.asList(quarters, quarterlyCounts));
        firstDashboard.put("yearly", Arrays.asList(years, yearlyCounts));
        LinkedHashMap<String, LinkedHashMap<String, List<List<String>>>> result = new LinkedHashMap<>();
        result.put("Dashboard 1:", firstDashboard);
        System.out.println(result);
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

        transaction.setBank(DataLoader.getBankByID(resultSet.getInt("bank_id")));  // bank (assuming sender_bank_id is the bank column)

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
