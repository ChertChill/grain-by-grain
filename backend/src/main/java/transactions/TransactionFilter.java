package transactions;

import authorization.User;
import database.DatabaseConnection;

import java.math.BigInteger;
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
            parameters.add(Integer.valueOf(filters.get("amount").getFirst()));
        }

        if (filters.containsKey("status_id")) {
            query.append(" AND status_id = ?");
            parameters.add(Integer.valueOf(filters.get("status_id").getFirst()));
        }

        if (filters.containsKey("bank")) {
            query.append(" AND bank = ?");
            parameters.add(filters.get("bank").getFirst());
        }

        if (filters.containsKey("amount_gt")) {
            query.append(" AND amount >= ?");
            parameters.add(Integer.valueOf(filters.get("amount_gt").getFirst()));
        }

        if (filters.containsKey("amount_lw")) {
            query.append(" AND amount <= ?");
            parameters.add(Integer.valueOf(filters.get("amount_lw").getFirst()));
        }

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(query.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i)); // Bind values safely
            }

            System.out.println(stmt.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        }
    }


    private Transaction mapResultSetToTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionID(resultSet.getLong("transaction_id"));  // transaction_id

        transaction.setUserID(resultSet.getLong("user_id"));  // user_id

        transaction.setLegalTypeID(resultSet.getInt("legal_type_id"));  // person_type_id (assuming this is legalTypeID)

        transaction.setTransactionDate(resultSet.getTimestamp("transaction_date").toLocalDateTime());  // transaction_date

        transaction.setTypeID(resultSet.getInt("type_id"));  // type_id

        transaction.setAmount(resultSet.getInt("amount"));  // amount (assuming it's stored as an integer)

        transaction.setStatusID(resultSet.getInt("status_id"));  // status_id

        transaction.setBank(resultSet.getString("bank"));  // bank (assuming sender_bank_id is the bank column)

        transaction.setAccountNumber(resultSet.getString("account_number"));  // account_number

        transaction.setRecipientNumber(resultSet.getString("recipient_number"));  // Recipient_number (assuming Reciiver_bank_id maps to recipient number)

        transaction.setRecipientTIN(resultSet.getLong("recipient_tin"));  // Recipient_tin (Reciiver_inn)

        transaction.setPhone(resultSet.getString("phone"));  // Reciiver_phone (phone)

        transaction.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());  // created_at

        transaction.setCategoryID(resultSet.getInt("category_id"));  // category_id

        transaction.setComment(resultSet.getString("comment"));  // comment


        return transaction;
    }
}
