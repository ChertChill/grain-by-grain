package api;

import io.javalin.*;
import io.javalin.http.Context;

import java.util.Map;


public class restAPI {

    public static void start() {
        Javalin app = Javalin.create(config -> {
            // 1) Always return JSON by default
            config.http.defaultContentType = "application/json";

            // 2) Enable the built‑in CORS plugin
            config.bundledPlugins.enableCors(cors -> {
                // allow any origin (for dev); for prod, replace anyHost() with allowHost(...)
                cors.addRule(rule -> rule.anyHost());
            });
        });

        int port = 7070;
        app.start(port);

        app.post("/api/login", restAPI::handleLogin);
        app.post("/api/register", restAPI::handleRegistration);
    }

    private static void handleRegistration(Context ctx) {
        RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
        Boolean registerResult = registerUser(req.name, req.email, req.password); //registerUser - PLACEHOLDER!!!
        if (registerResult) ctx.status(201).json(Map.of("success", true));
        else ctx.status(400);
    }

    private static void handleLogin(Context ctx) {
        LoginRequest req = ctx.bodyAsClass(LoginRequest.class);
        Boolean loginResult = loginUser(req.email, req.password); //loginUser - PLACEHOLDER!!!
        if (loginResult) ctx.status(201).json(Map.of("success", true));
        else ctx.status(400);
    }

    private static class LoginRequest {
        public String email;
        public String password;
    }

    private static class RegisterRequest {
        public String name;
        public String email;
        public String password;
    }

    //PLACEHOLDERS - ИМЕНИТЬ НА НАСТОЯЩИЕ МЕТОДЫ
    public static Boolean loginUser(String email, String password) {
        System.out.println("Вызов loginUser");
        System.out.println(email);
        System.out.println(password);
        return true;
    }

    public static Boolean registerUser(String name, String email, String password) {
        System.out.println("Вызов registerUser");
        System.out.println(name);
        System.out.println(email);
        System.out.println(password);
        return true;
    }
}
