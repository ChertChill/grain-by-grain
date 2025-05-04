package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static String databaseURL = "jdbc:postgresql://localhost:5432/DB_32";
    private static String databaseUser = "postgres";
    private static String databasePassword = "postgres";

    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    public static String getDatabaseURL() {
        return databaseURL;
    }

    public static void setDatabaseURL(String databaseURL) {
        DatabaseConnection.databaseURL = databaseURL;
    }

    public static String getDatabaseUser() {
        return databaseUser;
    }

    public static void setDatabaseUser(String databaseUser) {
        DatabaseConnection.databaseUser = databaseUser;
    }

    public static String getDatabasePassword() {
        return databasePassword;
    }

    public static void setDatabasePassword(String databasePassword) {
        DatabaseConnection.databasePassword = databasePassword;
    }

    public static void start() throws SQLException {
        config.setJdbcUrl(databaseURL);
        config.setUsername(databaseUser);
        config.setPassword(databasePassword);
        config.setMaximumPoolSize(10);
        config.setIdleTimeout(300_000);        // 5 minutes
        config.setConnectionTimeout(30_000);   // 30 seconds

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    // Добавление транзакции
    public static void addTransaction(int typeId, int senderBankId, int recipientBankId, BigDecimal amount,
                                      String comment, int statusId, long userId, String accountNumber,
                                      String recipientNumber, int legalTypeId, Date transactionDate,
                                      long recipientTin, Integer categoryId, String recipientPhone) throws SQLException {
        String query = "INSERT INTO transactions (type_id, sender_bank_id, recipient_bank_id, amount, comment, " +
                "status_id, created_at, user_id, account_number, recipient_number, legal_type_id, transaction_date, " +
                "recipient_tin, category_id, recipient_phone) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, typeId);
            stmt.setInt(2, senderBankId);
            stmt.setInt(3, recipientBankId);
            stmt.setBigDecimal(4, amount);
            stmt.setString(5, comment);
            stmt.setInt(6, statusId);
            stmt.setLong(7, userId);
            stmt.setString(8, accountNumber);
            stmt.setString(9, recipientNumber);
            stmt.setInt(10, legalTypeId);
            stmt.setDate(11, transactionDate);
            stmt.setLong(12, recipientTin);
            stmt.setObject(13, categoryId, Types.INTEGER);  // Это значение может быть null
            stmt.setString(14, recipientPhone);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении транзакции.");
            throw e;
        }
    }

    public static List<String> getAllTransactions() throws SQLException {
        List<String> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String info = "ID: " + rs.getLong("transaction_id") +
                        ", Amount: " + rs.getBigDecimal("amount") +
                        ", Status ID: " + rs.getInt("status_id");
                transactions.add(info);
            }
        }
        return transactions;
    }

    // Обновление транзакции
    public static boolean updateTransaction(long transactionId, BigDecimal amount, String comment,
                                            int statusId, Date transactionDate, String recipientPhone) throws SQLException {
        String query = "UPDATE transactions SET amount = ?, comment = ?, status_id = ?, transaction_date = ?, recipient_phone = ? WHERE transaction_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBigDecimal(1, amount);  // Устанавливаем сумму
            stmt.setString(2, comment);  // Устанавливаем комментарий
            stmt.setInt(3, statusId);  // Устанавливаем статус
            stmt.setDate(4, transactionDate);  // Устанавливаем дату
            stmt.setString(5, recipientPhone);  // Устанавливаем номер телефона
            stmt.setLong(6, transactionId);  // Устанавливаем ID транзакции

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении транзакции.");
            throw e;
        }
    }

    // Подтверждение транзакции
    public static boolean confirmTransaction(int transactionId) throws SQLException {
        String query = "UPDATE transactions SET status_id = 2 WHERE transaction_id = ?";  // статус 2 - подтверждено

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, transactionId);  // Устанавливаем ID транзакции

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при подтверждении транзакции.");
            throw e;
        }
    }

    // Удаление транзакции
    public static boolean deleteTransaction(int transactionId) throws SQLException {
        String query = "DELETE FROM transactions WHERE transaction_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, transactionId);  // Устанавливаем ID транзакции

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении транзакции.");
            throw e;
        }
    }
}
