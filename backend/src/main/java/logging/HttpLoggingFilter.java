package logging;

import authorization.JWTHandler;
import authorization.User;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.jsonwebtoken.JwtException;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class HttpLoggingFilter implements Handler {

    private final HttpLogRepository logRepository;


    public HttpLoggingFilter(HttpLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        // Фиксируем время начала обработки запроса
        ctx.attribute("startTime", System.currentTimeMillis());

        // Логируем базовые данные о запросе

        Long userId = null;
        String authHeader = null;
        String token = null;
        User currentUser = null;

        authHeader = ctx.header("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            currentUser = JWTHandler.getUser(token);
            userId = currentUser.getId();
        }

        String method = ctx.method().name();
        String path = ctx.path();
        String ip = ctx.ip();
        String userAgent = ctx.userAgent() != null ? ctx.userAgent() : "Unknown";
        LocalDateTime timestamp = LocalDateTime.now();

        // Сохраняем timestamp в контексте для последующего обновления
        ctx.attribute("logTimestamp", timestamp);

        logRepository.logRequest(userId, method, path, ip, userAgent, timestamp);
    }

    // Дополнительный метод для логирования после обработки запроса
    public static void logResponse(Context ctx, HttpLogRepository logRepository) {
        long startTime = (long) ctx.attribute("startTime");
        long durationMs = System.currentTimeMillis() - startTime;
        int status = ctx.status().getCode();
        LocalDateTime timestamp = (LocalDateTime) ctx.attribute("logTimestamp");

        Long userId = null;
        String authHeader = null;
        String token = null;
        User currentUser = null;


        authHeader = ctx.header("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            currentUser = JWTHandler.getUser(token);
            userId = currentUser.getId();
        }

        // Получаем сообщение об ошибке из контекста
        String error = ctx.status().getCode() >= 400 ?
                (String) ctx.attribute("errorMessage") : null;

        logRepository.logResponse(
                userId,
                ctx.method().name(),
                ctx.path(),
                status,
                durationMs,
                error,
                timestamp
        );
    }
}