public List<Transaction> getUserTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = (SELECT user_id FROM users WHERE email = ?)";
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                transactions.add(mapResultSetToTransactions(resultSet));
            }
        }
        return transactions;
    }