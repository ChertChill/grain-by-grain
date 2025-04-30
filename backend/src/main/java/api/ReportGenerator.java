package api;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseReportGenerator {

    private static final String DB_URL = ""; // URL вашей базы данных
    private static final String USER = ""; // Имя пользователя для базы данных
    private static final String PASS = ""; // Пароль для базы данных

    public void generateReport(String filePath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.addTitle("Отчет");

            // Получение данных из базы
            String query = "SELECT * FROM your_table"; // Замените на ваш SQL-запрос
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                // Генерация отчета на основе данных
                while (resultSet.next()) {
                    StringBuilder reportEntry = new StringBuilder();

                    // столбцы id, name и value в таблице
                    reportEntry.append("ID: ").append(resultSet.getInt("id")).append("\n");
                    reportEntry.append("Name: ").append(resultSet.getString("name")).append("\n");
                    reportEntry.append("Value: ").append(resultSet.getDouble("value")).append("\n");
                    reportEntry.append("---------------\n");
                    document.add(new com.itextpdf.text.Paragraph(reportEntry.toString()));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
            System.out.println("PDF отчет был успешно создан: " + filePath);
        }
    }

    public static void main(String[] args) {
        DatabaseReportGenerator reportGenerator = new DatabaseReportGenerator();
        reportGenerator.generateReport("report.pdf"); // путь, где вы хотите сохранить PDF
    }
}
