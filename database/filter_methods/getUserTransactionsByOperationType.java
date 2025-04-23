public List<Transaction> getUserTransactionsByOperationType(Long userId, Long typeId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = (SELECT user_id FROM users WHERE email = ?) AND type_id= ?";
        

try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
statement.setLong(2, type_id);


             try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                transactions.add(mapResultSetToTransactions(resultSet));
            }
        }
        return transactions;
    }