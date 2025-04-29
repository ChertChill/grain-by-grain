package transactions;

import authorization.User;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import database.DataLoader;
import database.DatabaseConnection;
import io.javalin.http.util.JsonEscapeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
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

    public void getTransactionsByTime(List<Transaction> transactions) {
        Map<LocalDate, Integer> weeklyTransactions = new LinkedHashMap<>();
        int currentWeeklyTransactions = 0;
        LocalDate startingDate = LocalDate.of(1970, 1, 1);
        int lastWeek = 0;
        int totalWeeks = 0;
        transactions.sort(Comparator.comparing(Transaction::getTransactionDate));
        for (Transaction transaction : transactions) {
            LocalDate transactionDate = LocalDate.from(transaction.getTransactionDate());
            int currentWeek = transactionDate.get(WeekFields.ISO.weekOfWeekBasedYear());
            currentWeeklyTransactions++;
            System.out.println("Current week: " + currentWeek);
            if (lastWeek == currentWeek) {
                currentWeeklyTransactions++;
            }
            else {
                int weekDifference = currentWeek - lastWeek;
                for (int i = 1; i <= weekDifference; i++) {
                    weeklyTransactions.put(startingDate.plusWeeks(totalWeeks + i), 0);
                }
                weeklyTransactions.put(startingDate.plusWeeks(currentWeek), currentWeeklyTransactions);
                currentWeeklyTransactions = 0;
                totalWeeks += weekDifference;
                lastWeek = currentWeek;
            }
        }
        System.out.println("Total weekly transactions: " + weeklyTransactions);
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
