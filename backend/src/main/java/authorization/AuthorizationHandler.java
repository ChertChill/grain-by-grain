package authorization;

import authorization.errors.RegistrationInputException;
import authorization.errors.IncorrectPasswordException;
import org.mindrot.jbcrypt.BCrypt;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

public class AuthorizationHandler {

    //секретный ключ, необходимый для подписи/верификации JWT-токенов
    //по хорошему, вынести в отдельный файл конфигурации
    static SecretKey key = Jwts.SIG.HS256.key().build();
    static final int tokenExpirationTime = 60;

    //логин пользователя по логину/паролю
    public String login(String username, String password) throws IncorrectPasswordException {// время жизни (в минутах) токена авторизации
        //достаем из БД хэшированный пароль, проверяем его с введенным
        String hashedPassword = getUserHashedPW(username);
        if (!BCrypt.checkpw(password, hashedPassword)) throw new IncorrectPasswordException("Wrong Password!");

        //грузим юзера из БД
        User loggedUser = loadUser(username);

        //устанавливаем конец жизни токена
        long now = System.currentTimeMillis();
        Date expirationDate = new Date(now + (tokenExpirationTime*60*1000));

        //создаем и возвращаем токен авторизации
        return Jwts.builder()
                .subject(loggedUser.getUsername())
                .id(String.valueOf(loggedUser.getId()))
                .claim("email", loggedUser.getEmail())
                .expiration(expirationDate)
                .signWith(key).compact();
    }

    // СДЕЛАТЬ РЕАЛЬНЫЙ ЗАПРОС В БД!!!
    //загрузка юзера из БД
    private User loadUser(String username) {
        return new User(1L, "Adam", "not_adam@gmail.com");
    }

    // СДЕЛАТЬ РЕАЛЬНЫЙ ЗАПРОС В БД!!!
    //загрузка пароля из БД (выкинуть IncorrectPasswordException если пользователя нет в базе)
    private String getUserHashedPW(String username) throws IncorrectPasswordException {
        return "$2a$10$AWSxun1qeBwcozpWD8AW1uGz6AnhHfOwQMc4dTMbRnB9qx3w6ctV.";
    }

    //регистрация пользователя
    public void register(String username, String password, String email) throws RegistrationInputException {
        //проверки на валидность введенного пароля/доступность логина
        checkPasswordValidity(password);
        checkNameValidity(username);
        checkEmailValidity(email);

        //хэшируем пароль и добавляем юзера в БД
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        insertUserIntoDB(username, hashedPassword, email);
    }

    // СДЕЛАТЬ РЕАЛЬНЫЙ ЗАПРОС В БД!!!
    //добавление юзера в БД
    private void insertUserIntoDB(String username, String password, String email) {
        return;
    }

    //СДЕЛАТЬ: логику проверки пароля на запрещенные символы, длину и прочее
    //проверка пароля на валидность
    private void checkPasswordValidity(String password) throws RegistrationInputException {
        if (password.length() < 3 || password.length() > 25)
            throw new RegistrationInputException("Пароль должен быть > 3 и < 25 символов.");
        // другие проверки
        // ...
    }

    private void checkNameValidity(String name) throws RegistrationInputException {
        if (name.length() < 2 || name.length() > 16)
            throw new RegistrationInputException("Имя пользователя должно быть > 2 и < 16 символов.");
        // другие проверки, в том числе на доступность имени в БД
        // ...
    }

    private void checkEmailValidity(String email) throws RegistrationInputException {
        //проверка e-mail на символы, доступность в БД и прочее
    }
}
