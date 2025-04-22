package api;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Scanner;


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
        public void toLogin() {
            if (email != null && password != null) {
                try {
                    if (email.equals(email) && password.equals(RegisterRequest.hashPassword(password))) {
                        System.out.println("ENTER IS DONE");
                    }
                    System.out.println("WRONG PASSWORD OR LOGIN");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }

    private static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public void toRegistrate() {
            Scanner scanner = new Scanner(System.in);
            email = scanner.nextLine().trim();
            password = scanner.nextLine().trim();
        }
        public static String hashPassword(String password) throws NoSuchAlgorithmException {
            MessageDigest hashedPassword = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = hashedPassword.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return password;
        }
    }

    //PLACEHOLDERS - ИМЕНИТЬ НА НАСТОЯЩИЕ МЕТОДЫ
    /*public static Boolean loginUser(String email, String password) {
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
    */

}
