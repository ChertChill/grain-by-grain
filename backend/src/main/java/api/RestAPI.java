package api;

import authorization.AuthorizationHandler;
import authorization.JWTHandler;
import authorization.User;
import authorization.errors.IncorrectPasswordException;
import authorization.errors.RegistrationInputException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.JwtException;
import transactions.DashboardGenerator;
import transactions.Transaction;
import transactions.TransactionFilter;
import transactions.TransactionSummary;
import database.Bank;
import database.Category;
import database.TransactionStatus;
import database.DataLoader;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RestAPI {

    //запуск АПИ
    public static void start() {
        Javalin app = Javalin.create(config -> {
            // делаем JSON стандартным типом возвратных значений
            config.http.defaultContentType = "application/json";

            // 2) включаем CORS-плагин
            config.bundledPlugins.enableCors(cors -> {
                // разрешаем коммуникацию со всех хостов (на проде потом перенастроить нужно будет)
                cors.addRule(rule -> rule.anyHost());
            });
        });

        int port = 7070;
        app.start(port);

        //доступные API-запросы
        app.post("/api/login", RestAPI::loginRequest);
        app.post("/api/register", RestAPI::registrationRequest);
        app.get("/api/check_user", RestAPI::checkUser);
        app.get("/api/get_transactions", RestAPI::getUserTransactions);
        app.get("/api/reference_data", RestAPI::getReferenceData);
        app.post("/api/create_transaction", RestAPI::createTransaction);
        app.put("/api/update_transaction/{id}", RestAPI::updateTransaction);
        app.put("/api/confirm_transaction/{id}", RestAPI::confirmTransaction);
        app.put("/api/delete_transaction/{id}", RestAPI::deleteTransaction);
    }

    private static String checkHeader(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }

    private static void getUserTransactions(Context ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String token = checkHeader(ctx);
            User currentUser = JWTHandler.getUser(token);
            TransactionFilter transactionFilter = new TransactionFilter();
            Map<String, List<String>> queryParams = ctx.queryParamMap();
            List<Transaction> selectedTransactions = transactionFilter.getUserTransactions(currentUser, queryParams);
            selectedTransactions.sort(Comparator.comparing(Transaction::getTransactionDate));
            response.put("transactions", selectedTransactions);

            response.put("summary", new TransactionSummary(selectedTransactions).toMap());

            LinkedHashMap<String, Object> dashboards = null;
            DashboardGenerator dashboardGenerator = new DashboardGenerator();
            //Это можно красивее сделать
            if (queryParams.containsKey("transaction_date" + transactionFilter.greater_identificator)) {
                dashboards = dashboardGenerator.generateDashboards(
                        LocalDateTime.parse(queryParams.get("transaction_date" + transactionFilter.greater_identificator).getFirst()),
                        selectedTransactions);
            } else if (queryParams.containsKey("transaction_date" + transactionFilter.less_identificator)) {
                dashboards = dashboardGenerator.generateDashboards(selectedTransactions.getFirst().getTransactionDate(),
                        selectedTransactions);
            } else if (queryParams.containsKey("transaction_date" + transactionFilter.num_identificator)) {
                dashboards = dashboardGenerator.generateDashboards(
                        LocalDateTime.parse(queryParams.get("transaction_date" + transactionFilter.num_identificator).getFirst()),
                        selectedTransactions);
            } else {
                dashboards = dashboardGenerator.generateDashboards(selectedTransactions.getFirst().getTransactionDate(),
                        selectedTransactions);
            }
            response.put("dashboards", dashboards);
            ctx.status(201).json(response);
        } catch (JwtException | SQLException e) {
            ctx.status(400).json(e.getMessage());
        }
    }

    //выдает всю информацию по авторизованному юзеру (через JWT-токен)
    private static void checkUser(Context ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String token = checkHeader(ctx);
            User currentUser = JWTHandler.getUser(token);
            response.put("valid", true);
            response.put("full_name", currentUser.getFullName());
            ctx.status(201).json(response);
        } catch (JwtException e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            ctx.status(400).json(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", "UNEXPECTED ERROR: " + e.getMessage());
            ctx.status(400).json(response);
        }
    }

    //обработка запроса регистрации
    private static void registrationRequest(Context ctx) {
        AuthorizationHandler authorizationHandler = new AuthorizationHandler();
        //парсим JSON из запроса в registerData
        registerData req = ctx.bodyAsClass(registerData.class);
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            //запрос на регистрацию в authorizationHandler, который проведет все проверки/хэш пароля
            String authToken = authorizationHandler.register(req.full_name, req.password, req.email);
            response.put("success", true);
            response.put("token", authToken);
            ctx.status(201).json(response);
        } catch (RegistrationInputException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            ctx.status(400).json(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "UNEXPECTED ERROR: " + e.getMessage());
            ctx.status(400).json(response);
        }
    }

    //обработка запроса логина
    private static void loginRequest(Context ctx) throws IncorrectPasswordException {
        AuthorizationHandler authorizationHandler = new AuthorizationHandler();
        loginData req = ctx.bodyAsClass(loginData.class);
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            //authorizationHandler либо возвращает JWT-токен, либо выкидывает ошибку.
            String authToken = authorizationHandler.login(req.email, req.password);
            response.put("success", true);
            response.put("token", authToken);
            response.put("full_name", JWTHandler.getUser(authToken).getFullName());

            ctx.status(201).json(response);
        } catch (IncorrectPasswordException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            ctx.status(400).json(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "UNEXPECTED ERROR: " + e.getMessage());
            ctx.status(400).json(response);
        }
    }

    //промежуточные классы, необходимые для парсинга JSONа. Не содержат никакой логики.
    private static class loginData {
        public String email;
        public String password;
    }

    private static class registerData {
        public String full_name;
        public String email;
        public String password;
    }


    //выдает всю информацию по справочникам
    private static void getReferenceData(Context ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String token = checkHeader(ctx);
            User currentUser = JWTHandler.getUser(token);
            
            // Get banks
            List<Bank> banks = DataLoader.getBanks();
            response.put("banks", banks);
            
            // Get categories
            List<Category> categories = DataLoader.getCategories();
            response.put("categories", categories);
            
            // Get transaction statuses
            List<TransactionStatus> statuses = DataLoader.getTransactionStatuses();
            response.put("statuses", statuses);
            
            ctx.status(200).json(response);
        } catch (JwtException e) {
            response.put("error", e.getMessage());
            ctx.status(400).json(response);
        }
    }

    // Обработка запроса на создание транзакции
    private static void createTransaction(Context ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String token = checkHeader(ctx);
            User currentUser = JWTHandler.getUser(token);

            // Parse request body
            Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
            
            // Validate phone number
            String phoneNumber = (String) requestBody.get("recipientPhone");
            if (phoneNumber == null || !phoneNumber.matches("^(\\+7|8)[0-9]{10}$")) {
                response.put("success", false);
                response.put("error", "Номер телефона должен начинаться с +7 или 8 и содержать 11 цифр");
                ctx.status(400).json(response);
                return;
            }
            
            // Create transaction
            Transaction transaction;
            try {
                transaction = Transaction.createFromRequest(requestBody, currentUser);
            } catch (Exception e) {
                response.put("success", false);
                response.put("error", "Ошибка при создании транзакции: " + e.getMessage());
                response.put("details", "Проверьте правильность всех полей формы");
                ctx.status(400).json(response);
                return;
            }

            // Save to database
            try {
                transaction.saveToDatabase();
            } catch (SQLException e) {
                response.put("success", false);
                response.put("error", "Ошибка при сохранении в базу данных: " + e.getMessage());
                response.put("details", "Проверьте правильность всех полей формы");
                ctx.status(400).json(response);
                return;
            }

            response.put("success", true);
            response.put("message", "Transaction created successfully");
            ctx.status(201).json(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Неожиданная ошибка: " + e.getMessage());
            response.put("details", "Проверьте правильность всех полей формы");
            ctx.status(400).json(response);
        }
    }

    // Обработка запроса на обновление транзакции
    private static void updateTransaction(Context ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String token = checkHeader(ctx);
            User currentUser = JWTHandler.getUser(token);

            // Get transaction ID from path parameter
            String transactionId = ctx.pathParam("id");
            if (transactionId == null || transactionId.isEmpty()) {
                response.put("success", false);
                response.put("error", "ID транзакции не указан");
                ctx.status(400).json(response);
                return;
            }

            // Parse request body
            Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
            
            // Validate phone number
            String phoneNumber = (String) requestBody.get("recipientPhone");
            if (phoneNumber == null || !phoneNumber.matches("^(\\+7|8)[0-9]{10}$")) {
                response.put("success", false);
                response.put("error", "Номер телефона должен начинаться с +7 или 8 и содержать 11 цифр");
                ctx.status(400).json(response);
                return;
            }

            // Update transaction
            try {
                Transaction transaction = Transaction.updateFromRequest(transactionId, requestBody, currentUser);
                transaction.updateInDatabase();
                
                response.put("success", true);
                response.put("message", "Transaction updated successfully");
                ctx.status(200).json(response);
            } catch (SQLException e) {
                response.put("success", false);
                response.put("error", "Ошибка при обновлении транзакции: " + e.getMessage());
                response.put("details", "Проверьте правильность всех полей формы");
                ctx.status(400).json(response);
            } catch (Exception e) {
                response.put("success", false);
                response.put("error", "Ошибка при обновлении транзакции: " + e.getMessage());
                response.put("details", "Проверьте правильность всех полей формы");
                ctx.status(400).json(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Неожиданная ошибка: " + e.getMessage());
            response.put("details", "Проверьте правильность всех полей формы");
            ctx.status(400).json(response);
        }
    }

    // Обработка запроса на подтверждение транзакции
    private static void confirmTransaction(Context ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String token = checkHeader(ctx);
            User currentUser = JWTHandler.getUser(token);

            // Get transaction ID from path parameter
            String transactionId = ctx.pathParam("id");
            if (transactionId == null || transactionId.isEmpty()) {
                response.put("success", false);
                response.put("error", "ID транзакции не указан");
                ctx.status(400).json(response);
                return;
            }

            // Update transaction status to "Подтвержденная" (status ID 2)
            try {
                Transaction transaction = Transaction.getById(transactionId);
                if (transaction == null) {
                    response.put("success", false);
                    response.put("error", "Транзакция не найдена");
                    ctx.status(404).json(response);
                    return;
                }

                transaction.setStatus(new TransactionStatus(2, "Подтвержденная", false));
                transaction.updateInDatabase();
                
                response.put("success", true);
                response.put("message", "Transaction confirmed successfully");
                ctx.status(200).json(response);
            } catch (SQLException e) {
                response.put("success", false);
                response.put("error", "Ошибка при подтверждении транзакции: " + e.getMessage());
                ctx.status(400).json(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Неожиданная ошибка: " + e.getMessage());
            ctx.status(400).json(response);
        }
    }

    // Обработка запроса на удаление транзакции
    private static void deleteTransaction(Context ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String token = checkHeader(ctx);
            User currentUser = JWTHandler.getUser(token);

            // Get transaction ID from path parameter
            String transactionId = ctx.pathParam("id");
            if (transactionId == null || transactionId.isEmpty()) {
                response.put("success", false);
                response.put("error", "ID транзакции не указан");
                ctx.status(400).json(response);
                return;
            }

            // Update transaction status to "Платеж удален" (status ID 6)
            try {
                Transaction transaction = Transaction.getById(transactionId);
                if (transaction == null) {
                    response.put("success", false);
                    response.put("error", "Транзакция не найдена");
                    ctx.status(404).json(response);
                    return;
                }

                transaction.setStatus(new TransactionStatus(6, "Платеж удален", true));
                transaction.updateInDatabase();
                
                response.put("success", true);
                response.put("message", "Transaction deleted successfully");
                ctx.status(200).json(response);
            } catch (SQLException e) {
                response.put("success", false);
                response.put("error", "Ошибка при удалении транзакции: " + e.getMessage());
                ctx.status(400).json(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Неожиданная ошибка: " + e.getMessage());
            ctx.status(400).json(response);
        }
    }

}
