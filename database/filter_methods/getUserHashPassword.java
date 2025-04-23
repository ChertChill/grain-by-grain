public String getUserHashPassword(String email) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE email = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("password_hash");
                }
            }
        }
        return null;
    }