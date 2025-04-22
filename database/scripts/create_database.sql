-- Создание базы данных
CREATE DATABASE "GrainByGrain"
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'ru-RU'
    LC_CTYPE = 'ru-RU'
	LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
	IS_TEMPLATE = False;
COMMENT ON DATABASE "GrainByGrain"
    IS 'База данных вэб-приложения финансового мониторинга и учёта';