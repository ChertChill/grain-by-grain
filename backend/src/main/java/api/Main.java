package api;

import io.javalin.Javalin;
import database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Создание и запуск Javalin-сервера на порту 8080
        Javalin app = Javalin.create().start(8080);  // Запуск сервера на порту 8080

        // Обработка GET запроса по корневому пути
        app.get("/", ctx -> ctx.result("Есть контакт!"));

        // Пример API маршрута: POST запрос на создание пользователя
        app.post("/api/users", ctx -> {
            String name = ctx.formParam("full_name");
            ctx.result("Создано новое имя пользователя: " + name);
        });

        // Пример API маршрута: GET запрос для получения информации о пользователе
        app.get("/api/users/{id}", ctx -> {
            String userId = ctx.pathParam("id");
            ctx.result("User info for ID: " + userId);
        });

        // Пример API маршрута: PUT запрос для обновления пользователя
        app.put("/api/users/{id}", ctx -> {
            String userId = ctx.pathParam("id");
            String newName = ctx.formParam("name");
            ctx.result("Updated user " + userId + " with new name: " + newName);
        });

        // Пример API маршрута: DELETE запрос для удаления пользователя
        app.delete("/api/users/{id}", ctx -> {
            String userId = ctx.pathParam("id");
            ctx.result("Deleted user with ID: " + userId);
        });

        System.out.println("API сервер запущен на порту 8080");

        // Подключение к базе данных
        try {
            DatabaseConnection.start();
        } catch (SQLException e) {
            System.out.println("Unable to connect to database.");
            e.printStackTrace();
        }

        // Маршрут для создания транзакции
        app.post("/api/transactions", ctx -> {
            String typeIdParam = ctx.formParam("type_id");
            String senderBankIdParam = ctx.formParam("sender_bank_id");
            String recipientBankIdParam = ctx.formParam("recipient_bank_id");
            String amountParam = ctx.formParam("amount");

            if (typeIdParam == null || senderBankIdParam == null || recipientBankIdParam == null || amountParam == null) {
                ctx.status(400).result("Required parameters are missing.");
                return;
            }

            try {
                int typeId = Integer.parseInt(typeIdParam);
                int senderBankId = Integer.parseInt(senderBankIdParam);
                int recipientBankId = Integer.parseInt(recipientBankIdParam);
                BigDecimal amount = new BigDecimal(amountParam);
                String comment = ctx.formParam("comment");
                int statusId = Integer.parseInt(ctx.formParam("status_id"));
                long userId = Long.parseLong(ctx.formParam("user_id"));
                String accountNumber = ctx.formParam("account_number");
                String recipientNumber = ctx.formParam("recipient_number");
                int legalTypeId = Integer.parseInt(ctx.formParam("legal_type_id"));
                Date transactionDate = Date.valueOf(ctx.formParam("transaction_date"));
                long recipientTin = Long.parseLong(ctx.formParam("recipient_tin"));
                Integer categoryId = ctx.formParam("category_id") != null ? Integer.parseInt(ctx.formParam("category_id")) : null;
                String recipientPhone = ctx.formParam("recipient_phone");

                // Логика добавления транзакции в базу данных
                DatabaseConnection.addTransaction(typeId, senderBankId, recipientBankId, amount, comment, statusId,
                        userId, accountNumber, recipientNumber, legalTypeId, transactionDate,
                        recipientTin, categoryId, recipientPhone);
                ctx.result("Transaction created successfully.");
            } catch (SQLException | NumberFormatException e) {
                ctx.status(500).result("Failed to create transaction.");
                e.printStackTrace();
            }
        });

        app.get("/api/transactions", ctx -> {
            try {
                List<String> transactions = DatabaseConnection.getAllTransactions();
                if (transactions.isEmpty()) {
                    ctx.result("No transactions found.");
                } else {
                    ctx.result(String.join("\n", transactions));
                }
            } catch (SQLException e) {
                ctx.status(500).result("Failed to fetch transactions.");
                e.printStackTrace();
            }
        });


        // Маршрут для обновления транзакции
        app.put("/api/transactions/{id}", ctx -> {
            long transactionId = Long.parseLong(ctx.pathParam("id"));
            BigDecimal amount = new BigDecimal(ctx.formParam("amount"));
            String comment = ctx.formParam("comment");
            int statusId = Integer.parseInt(ctx.formParam("status_id"));
            Date transactionDate = Date.valueOf(ctx.formParam("transaction_date"));
            String recipientPhone = ctx.formParam("recipient_phone");

            try {
                boolean updated = DatabaseConnection.updateTransaction(transactionId, amount, comment, statusId, transactionDate, recipientPhone);
                if (updated) {
                    ctx.result("Transaction updated successfully.");
                } else {
                    ctx.status(404).result("Transaction not found.");
                }
            } catch (SQLException e) {
                ctx.status(500).result("Failed to update transaction.");
                e.printStackTrace();
            }
        });

        // Маршрут для подтверждения транзакции
        app.patch("/api/transactions/{id}/confirm", ctx -> {
            long transactionId = Long.parseLong(ctx.pathParam("id"));
            try {
                DatabaseConnection.confirmTransaction((int) transactionId);
                ctx.result("Transaction confirmed.");
            } catch (SQLException e) {
                ctx.status(500).result("Failed to confirm transaction.");
                e.printStackTrace();
            }
        });

        // Маршрут для удаления транзакции
        app.delete("/api/transactions/{id}", ctx -> {
            long transactionId = Long.parseLong(ctx.pathParam("id"));
            try {
                DatabaseConnection.confirmTransaction((int) transactionId);
                ctx.result("Transaction deleted successfully.");
            } catch (SQLException e) {
                ctx.status(500).result("Failed to delete transaction.");
                e.printStackTrace();
            }
        });
    }
}
