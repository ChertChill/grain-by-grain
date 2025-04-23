public List<Transaction> getUserTransactionsByOperationAmount(Long userId, Double amount) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = (SELECT user_id FROM users WHERE email = ?) AND amount = ?";
        

try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
statement.setDouble(2, amount);


             try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                transactions.add(mapResultSetToTransactions(resultSet));
            }
        }
        return transactions;
    }