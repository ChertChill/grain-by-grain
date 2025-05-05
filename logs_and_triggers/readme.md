1) RestAPI_добавление_логов.java  
настройка логов HTTP-запросов в Javalin  
2) logging.zip  
пакет классов для логирования HTTP-запросов  
3) триггеры_все.zip  
файлы с кодом для создания таблиц users_audit, transactions_audit, триггерных функций и самих триггеров  
4) all_triggers.sql  
sql-код, собранный в один скрипт  




В классе RestAPI добавляем член logRepository  
после старта Javalin выполняем вызов  app.before(...), app.after(...) и app.exception(...) как приведено ниже:  

```java
public class RestAPI {
    private static final HttpLogRepository logRepository = new HttpLogRepository();

    //запуск АПИ
    public static void start() {

        Javalin app = Javalin.create(config -> {
          .....
        });
        int port = 7070;
        app.start(port);


        app.before(new HttpLoggingFilter(logRepository));

        app.after(ctx -> HttpLoggingFilter.logResponse(ctx, logRepository)); // <-- Логируем ответы

        // Обработчик ошибок для сохранения сообщений
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(Map.of("error", e.getMessage()));
            ctx.attribute("errorMessage", e.getMessage()); // Для логов
        });
```
