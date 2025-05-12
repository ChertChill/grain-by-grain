package logging;

import database.DatabaseConnection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class ExportCSV {
    public static File export(String tableName) {

        String sql = "SELECT * FROM " + tableName;

        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name");
        }

        String csvFilePath = tableName + ".csv";

        File logFile = null;
        FileWriter fileWriter = null;

        ResultSet resultSet = null;
        PreparedStatement stmt = null;

        try {
            stmt = DatabaseConnection.getConnection().prepareStatement(sql);
//            stmt.setString(1, tableName);

            resultSet = stmt.executeQuery();

        } catch ( SQLException e) {
            System.err.println("Failed to get " + tableName);
            System.err.println("ERROR: " + e.getMessage());
        }



        try
        {
            logFile = new File(csvFilePath);
            fileWriter = new FileWriter(logFile);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                fileWriter.append(metaData.getColumnName(i));
                if (i < columnCount) {
                    fileWriter.append(",");
                }
            }
            fileWriter.append("\n");

            // 5. Записываем данные
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    // Экранируем запятые и кавычки в значениях
                    if (value != null && (value.contains(",") || value.contains("\""))) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    fileWriter.append(value != null ? value : "");
                    if (i < columnCount) {
                        fileWriter.append(",");
                    }
                }
                fileWriter.append("\n");
            }

            System.out.println("Данные успешно экспортированы в " + csvFilePath);

        } catch ( SQLException e) {
            System.err.println("Failed to export " + tableName);
            System.err.println("ERROR: " + e.getMessage());
        } catch ( IOException e)
        {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (stmt != null) stmt.close();
                if (fileWriter != null) fileWriter.close();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        return logFile;
    }
}