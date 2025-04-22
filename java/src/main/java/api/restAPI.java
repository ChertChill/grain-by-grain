package api;

import io.javalin.*;
import io.javalin.http.Context;

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

        int port = 7000;
        app.start(port);

        app.post("/api/login", restAPI::handleLogin);
        app.post("/api/register", restAPI::handleRegistration);
    }

    private static void handleRegistration(Context ctx) {
        RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
        Boolean registerResult = registerUser(req.username, req.password, req.email); //registerUser - PLACEHOLDER!!!
        if (registerResult) ctx.status(201);
        else ctx.status(400);

    }

    private static void handleLogin(Context ctx) {
        LoginRequest req = ctx.bodyAsClass(LoginRequest.class);
        String loginResult = loginUser(req.username, req.password); //loginUser - PLACEHOLDER!!!
        if (loginResult == null) ctx.status(400);
        else ctx.status(200).json(loginResult);
    }

    private static class LoginRequest {
        public String username;
        public String password;
    }

    private static class RegisterRequest {
        public String username;
        public String password;
        public String email;
    }

    //PLACEHOLDERS - ИМЕНИТЬ НА НАСТОЯЩИЕ МЕТОДЫ
    public static String loginUser(String username, String password) {
        return null;
    }

    public static Boolean registerUser(String username, String password, String email) {
        return true;
    }
}
