## Содержимое папки
1) RestAPI_добавление_логов.java  
настройка логов HTTP-запросов в Javalin  
2) logging.zip  
пакет классов для логирования HTTP-запросов  
3) триггеры_все.zip  
файлы с кодом для создания таблиц users_audit, transactions_audit, триггерных функций и самих триггеров  
4) all_triggers.sql  
sql-код, собранный в один скрипт
5) CREATE_http_logs.sql
SQL для создания таблицы http_logs в БД


## Настройка создания и экспорта логов
В базе данных создаем таблицу http_logs для логирования HTTP-запросов (CREATE_http_logs.sql).  
Для логирования изменений в БД создаем таблицы users_audit, transactions_audit, триггерные функций и сами триггеры (all_triggers.sql).  
В классе RestAPI подключаем пакет logging (logging.zip).  
Добавляем член logRepository.  
После старта Javalin выполняем вызов  **app.before**(...), **app.after**(...) и **app.exception**(...) как приведено ниже:  
    - <details>
        <summary>RestAPI.java</summary>
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
      </details>```

## Экспорт логов
Для экспорта логов http-запросов, изменения таблиц users_audit или transactions_audit следует:  
- выполнить вход (энд-поинт /api/login)  
- получить токен  
- выполнить запрос /api/get_logs с указанием токена авторизации и имени таблицы tableName в параметре json  
{http_logs, users_audit, transactions_audit}  
    - <details>
        <summary>пример обращения к энд-поинту /api/get_logs</summary>
            ```cmd
            curl.exe -v -X GET http://localhost:7070/api/get_logs -H "Authorization: Bearer [token]" -H "Content-Type: application/json"  -d '{\"tableName\":\"http_logs\"}' -o data.csv 
      </details>```
- в параметре запроса -o указывается имя файла, в который будет выгружен запрошенный лог.  
