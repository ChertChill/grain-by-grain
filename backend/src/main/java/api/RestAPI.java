package api;

import authorization.AuthorizationHandler;
import authorization.JWTHandler;
import authorization.User;
import authorization.errors.IncorrectPasswordException;
import authorization.errors.RegistrationInputException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.JwtException;
import transactions.TransactionFilter;

import java.sql.SQLException;
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
        app.get("/api/get_user_transactions", RestAPI::getUserTransactions);
    }

    private static void getUserTransactions(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(401).json("Missing or invalid Authorization header");
            return;
        }
        String token = authHeader.substring(7);

        try {
            User currentUser = JWTHandler.getUser(token);
            TransactionFilter transactionFilter = new TransactionFilter();
            Map<String, List<String>> queryParams = ctx.queryParamMap();
            ctx.status(201).json(transactionFilter.getUserTransactions(currentUser, queryParams));
        } catch (JwtException | SQLException e) {
            ctx.status(400).json(e.getMessage());
        }
    }

    //выдает всю информацию по авторизованному юзеру (через JWT-токен)
    private static void checkUser(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(400).json("Missing or invalid Authorization header");
            return;
        }
        String token = authHeader.substring(7);
        Map<String, Object> response = new LinkedHashMap<>();
        try {
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

}
