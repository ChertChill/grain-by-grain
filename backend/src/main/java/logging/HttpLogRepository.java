package logging;

import database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class HttpLogRepository {
    public void logRequest_00(String method, String path, String ip, String userAgent, LocalDateTime timestamp) {
        // Пример: вывод в консоль
        System.out.printf(
                "[HTTP] %s | %s | %s | %s | %s%n",
                timestamp, method, path, ip, userAgent
        );
    }

    public void logRequest(Long userId, String method, String path, String ip, String userAgent, LocalDateTime timestamp) {
        System.out.printf(
                "[REQUEST]  %s | %-6s | %-20s | %-15s | %s%n",
                timestamp, method, path, ip, userAgent
        );

        // Сохранение в БД
        String sql = "INSERT INTO http_logs (user_id, request_method, request_path, request_ip, " +
                "request_user_agent, request_timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if(userId != null) stmt.setLong(1, userId);
            else stmt.setNull(1, Types.BIGINT);

            stmt.setString(2, method);
            stmt.setString(3, path);
            stmt.setString(4, ip);
            stmt.setString(5, userAgent);
            stmt.setTimestamp(6, Timestamp.valueOf(timestamp));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to log request: " + e.getMessage());
        }

    }

    public void logResponse(Long userId, String method, String path, int status, long durationMs, String error, LocalDateTime timestamp) {
        String errorLog = error != null ? " | Error: " + error : "";
        System.out.printf(
                "[RESPONSE] %s | %-6s | %-20s | Status: %d | Time: %d ms%s%n",
                timestamp, method, path, status, durationMs, errorLog
        );

        // Обновление записи в БД
        String sql = "WITH last_log AS (" +
                "SELECT id FROM http_logs " +
                "WHERE request_method = ? AND request_path = ? " +
                "AND request_timestamp = ? " +
                "ORDER BY id DESC LIMIT 1" +
                ") " +
                "UPDATE http_logs SET response_status = ?, response_time_ms = ?, " +
                "error_message = ?, user_id = ? WHERE id IN (SELECT id FROM last_log)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, method);
            stmt.setString(2, path);
            stmt.setTimestamp(3, Timestamp.valueOf(timestamp));
            stmt.setInt(4, status);
            stmt.setLong(5, durationMs);
            stmt.setString(6, error);

            if(userId != null) stmt.setLong(7, userId);
            else stmt.setNull(7, Types.BIGINT);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to log response: " + e.getMessage());
        }
    }
}