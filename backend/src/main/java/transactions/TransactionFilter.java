package transactions;

import authorization.User;
import database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionFilter {
    public List<Transaction> getUserTransactions(User user, Map<String, List<String>> filters) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");
        List<Object> parameters = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        parameters.add(user.getId());

        if (filters.containsKey("amount")) {
            query.append(" AND amount = ?");
            parameters.add(filters.get("amount").get(0)); // Assuming single value
        }

        if (filters.containsKey("type")) {
            query.append(" AND type = ?");
            parameters.add(filters.get("type").get(0));
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


    private Transaction mapResultSetToTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(resultSet.getLong("transaction_id"));

        transaction.setSenderBankId(resultSet.getLong("sender_bank_id"));

        transaction.setRecieverBankId(resultSet.getLong("Reciever_bank_id"));


        transaction.setStatusId(resultSet.getLong("status_id"));

        transaction.setRecieverInn(String.valueOf(resultSet.getLong("Reciever_inn")));

        transaction.setPersonTypeId(resultSet.getLong("person_type_id"));

        transaction.setAccountNumber(resultSet.getString("account_number"));

        transaction.setRecieverAccount(resultSet.getString("Reciever_account"));

        transaction.setRecieverPhone(resultSet.getString("Reciever_phone"));

        transaction.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

        transaction.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());

        transaction.setTransactionDate(resultSet.getTimestamp("transaction_date").toLocalDateTime());
        transaction.setCategoryId(resultSet.getLong("category_id"));
        transaction.setTypeId(resultSet.getLong("type_id"));
        transaction.setAmount(resultSet.getDouble("amount"));
        transaction.setComment(resultSet.getString("comment"));

        return transaction;
    }
}
