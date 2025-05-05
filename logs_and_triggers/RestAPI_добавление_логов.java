package api;


public class RestAPI {

    // добавляем logRepository
    private static final HttpLogRepository logRepository = new HttpLogRepository();

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


        // настраиваем логирование запросов
        app.before(new HttpLoggingFilter(logRepository));
        app.after(ctx -> HttpLoggingFilter.logResponse(ctx, logRepository)); // <-- Логируем ответы
        // Обработчик ошибок для сохранения сообщений
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(Map.of("error", e.getMessage()));
            ctx.attribute("errorMessage", e.getMessage()); // Для логов
        });


        //доступные API-запросы
        app.post("/api/login", RestAPI::loginRequest);
        app.post("/api/register", RestAPI::registrationRequest);
        app.get("/api/check_user", RestAPI::checkUser);
        app.get("/api/get_transactions", RestAPI::getUserTransactions);

    }


}
