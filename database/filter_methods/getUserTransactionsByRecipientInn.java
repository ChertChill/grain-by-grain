public List<Transaction> getUserTransactionsByRecipientInn(Long userId, String recipientInn) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = (SELECT user_id FROM users WHERE email = ?) AND recipient_inn= ?";
        

try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
statement.setString(2, recipientInn);


             try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                transactions.add(mapResultSetToTransactions(resultSet));
            }
        }
        return transactions;
    }