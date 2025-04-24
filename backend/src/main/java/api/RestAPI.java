package api;

import authorization.AuthorizationHandler;
import authorization.JWTHandler;
import authorization.User;
import authorization.errors.IncorrectPasswordException;
import authorization.errors.RegistrationInputException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.JwtException;

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
    }

    //выдает всю информацию по авторизованному юзеру (через JWT-токен)
    private static void checkUser(Context ctx) {
        String token = ctx.body();
        try {
            User currentUser = JWTHandler.getUser(token);
            ctx.status(201).json(currentUser);
        } catch (JwtException e) {
            ctx.status(400).json(e.getMessage());
        }
    }

    //обработка запроса регистрации
    private static void registrationRequest(Context ctx) {
        AuthorizationHandler authorizationHandler = new AuthorizationHandler();
        //парсим JSON из запроса в registerData
        registerData req = ctx.bodyAsClass(registerData.class);
        try {
            //запрос на регистрацию в authorizationHandler, который проведет все проверки/хэш пароля
            authorizationHandler.register(req.name, req.password, req.email);
            ctx.status(201).json(Map.of("success", true));
        } catch (RegistrationInputException e) {
            ctx.status(400).json(e.getMessage());
        } catch (Exception e) {
            ctx.status(400).json("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    //обработка запроса логина
    private static void loginRequest(Context ctx) throws IncorrectPasswordException {
        AuthorizationHandler authorizationHandler = new AuthorizationHandler();
        loginData req = ctx.bodyAsClass(loginData.class);
        try {
            //authorizationHandler либо возвращает JWT-токен, либо выкидывает ошибку.
            String loginResult = authorizationHandler.login(req.name, req.password);
            ctx.status(201).json(Map.of("success", loginResult));
        } catch (IncorrectPasswordException e) {
            ctx.status(400).json(e.getMessage());
        }  catch (Exception e) {
            ctx.status(400).json("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    //промежуточные классы, необходимые для парсинга JSONа. Не содержат никакой логики.
    private static class loginData {
        public String name;
        public String password;
    }

    private static class registerData {
        public String name;
        public String email;
        public String password;
    }

}
