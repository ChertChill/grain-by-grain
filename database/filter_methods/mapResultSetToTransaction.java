// Вспомогательный метод для маппинга ResultSet в Transaction


    private Transaction mapResultSetToTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(resultSet.getLong("transaction_id"));

transaction.setSenderBankId(resultSet.getLong("sender_bank_id"));

transaction.setRecipientBankId(resultSet.getLong("recipient_bank_id"));


transaction.setStatusId(resultSet.getLong("status_id"));



transaction.setRecipientInn(resultSet.getLong("recipient_inn"));

transaction.setPersonTypeId(resultSet.getLong("person_type_id"));

transaction.setAccountNumber(resultSet.getString("account_number"));

transaction.setRecipientAccount(resultSet.getString("recipient_account"));

transaction.setRecipientPhone(resultSet.getString("recipient_phone"));

transaction.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

transaction.setCreatedBy(resultSet.getLong("created_by"));

transaction.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());

transaction.setUpdatedBy(resultSet.getLong("updated_by"));

transaction.setTransactionDate(resultSet.getTimestamp("transaction_date").toLocalDateTime());
        transaction.setCategoryId(resultSet.getLong("category_id"));
        transaction.setTypeId(resultSet.getLong("type_id"));
        transaction.setAmount(resultSet.getDouble("amount"));
        transaction.setComnent(resultSet.getString("comment"));
        
        return transaction;
    }
}