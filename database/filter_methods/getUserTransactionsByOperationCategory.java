public List<Transaction> getUserTransactionsByOperationCategory(Long userId, Long categoryId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = (SELECT user_id FROM users WHERE email = ?) AND category_id= ?";
        

try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
statement.setLong(2, category_id);


             try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                transactions.add(mapResultSetToTransactions(resultSet));
            }
        }
        return transactions;
    }