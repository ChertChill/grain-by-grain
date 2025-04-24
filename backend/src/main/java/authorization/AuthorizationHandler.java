package authorization;

import authorization.errors.RegistrationInputException;
import authorization.errors.IncorrectPasswordException;
import database.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.sql.*;
import java.util.Date;

public class AuthorizationHandler {

    //секретный ключ, необходимый для подписи/верификации JWT-токенов
    //по хорошему, вынести в отдельный файл конфигурации
    static SecretKey key = Jwts.SIG.HS256.key().build();
    static final int tokenExpirationTime = 60;

    //логин пользователя по логину/паролю
    public String login(String user_name, String password) throws IncorrectPasswordException, SQLException {// время жизни (в минутах) токена авторизации
        //достаем из БД хэшированный пароль, проверяем его с введенным
        String hashedPassword = getUserHashedPW(user_name);
        if (!BCrypt.checkpw(password, hashedPassword)) throw new IncorrectPasswordException("Wrong Password!");

        //грузим юзера из БД
        User loggedUser = loadUser(user_name);

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

    //загрузка юзера из БД
    private User loadUser(String user_name) throws SQLException {
        String sql =
                "SELECT user_id, user_name, email " +
                        "FROM users " +
                        "WHERE user_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user_name);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("User not found!");
                }
                long id = rs.getInt   ("user_id");
                String user = rs.getString("user_name");
                String mail = rs.getString("email");

                return new User(id, user, mail);
            }
        }
    }

    //загрузка пароля из БД
    private String getUserHashedPW(String user_name) throws IncorrectPasswordException, SQLException {
        String sql = "SELECT password_hash FROM users WHERE user_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, user_name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new IncorrectPasswordException("User not found!");

                return resultSet.getString("password_hash");
            }
        }
    }

    //регистрация пользователя
    public void register(String user_name, String password, String email) throws RegistrationInputException, SQLException {
        //проверки на валидность введенного пароля/доступность логина
        checkPasswordValidity(password);
        checkNameValidity(user_name);
        checkEmailValidity(email);
        checkNameEmailExistence(user_name, email);

        //хэшируем пароль и добавляем юзера в БД
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        insertUserIntoDB(user_name, hashedPassword, email);
    }

    //добавление юзера в БД
    private void insertUserIntoDB(String user_name, String password, String email) throws SQLException {
        String sql = "INSERT INTO users (user_name, password_hash, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameters in the PreparedStatement
            pstmt.setString(1, user_name);   // Set user_name
            pstmt.setString(2, password);   // Set password (hashed password in real cases)
            pstmt.setString(3, email);      // Set email

            // Execute the update
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Registration successful!");
            }

        }
    }

    //СДЕЛАТЬ: логику проверки пароля на запрещенные символы, длину и прочее
    //проверка пароля на валидность
    private void checkPasswordValidity(String password) throws RegistrationInputException {
        if (password.length() < 3 || password.length() > 25)
            throw new RegistrationInputException("Пароль должен быть > 3 и < 25 символов.");
        // другие проверки
        // ...
    }

    private void checkNameEmailExistence(String user_name, String email) throws RegistrationInputException, SQLException {
        String sql =
                "SELECT " +
                        "  EXISTS(SELECT 1 FROM users WHERE user_name = ?) AS username_exists, " +
                        "  EXISTS(SELECT 1 FROM users WHERE email     = ?) AS email_exists";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user_name);
            ps.setString(2, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new RegistrationInputException("Either Username or Email already exists!");
                }
            }
        }
    }

    private void checkNameValidity(String name) throws RegistrationInputException {
        if (name.length() < 2 || name.length() > 16)
            throw new RegistrationInputException("Имя пользователя должно быть > 2 и < 16 символов.");
        // другие проверки
        // ...
    }

    private void checkEmailValidity(String email) throws RegistrationInputException {
        //проверка e-mail на символы
    }
}
