package database;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static String databaseURL = "jdbc:postgresql://localhost:5432/GrainByGrain";
    private static String databaseUser = "postgres";
    private static String databasePassword = "1";

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
}
