package transactions;

import authorization.User;
import database.Bank;
import database.DataLoader;
import database.DatabaseConnection;

import java.sql.Connection;
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
    public final String bigint_identificator = "-bnum";

    public List<Transaction> getUserTransactions(User user, Map<String, List<String>> filters) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");
        List<Object> parameters = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        parameters.add(user.getId());

        //применяем фильтры по параметрам
        if (!filters.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue().getFirst();

                String operator = "=";
                boolean toInt = false;
                boolean toBigInt = false;

                //если присутствуют параметры gt/lw/num, то изменяем меняем БД запрос и меняем ключи
                //нужно для того, чтобы избежать Mismatch у джавы/параметров
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
                    toInt = true;
                } else if (key.contains(bigint_identificator)) {
                    key = key.replace(bigint_identificator, "");
                    toBigInt = true;
                }

                //изменяем парсинг параметров в зависимости от их типа
                if (key.contains("transaction_date") || key.contains("created_at"))
                    parameters.add(LocalDateTime.parse(val));
                else if (toBigInt) parameters.add(Long.parseLong(val));
                else if (toInt) parameters.add(Integer.parseInt(val));
                else parameters.add(val);

                query.append(" AND ").append(key).append(" ").append(operator).append(" ?");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            // 1) bind all parameters first
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            // 2) then execute
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                return transactions;
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
