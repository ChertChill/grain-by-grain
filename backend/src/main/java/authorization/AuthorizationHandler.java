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

    //логин пользователя по логину/паролю
    public String login(String fullName, String password) throws IncorrectPasswordException, SQLException {// время жизни (в минутах) токена авторизации
        //достаем из БД хэшированный пароль, проверяем его с введенным
        String hashedPassword = getUserHashedPW(fullName);
        if (!BCrypt.checkpw(password, hashedPassword)) throw new IncorrectPasswordException("Wrong Password!");

        //грузим юзера из БД
        User loggedUser = loadUser(fullName);

        //устанавливаем конец жизни токена
        return JWTHandler.createUserToken(loggedUser);
    }

    //загрузка юзера из БД
    private User loadUser(String email) throws SQLException {
        String sql =
                "SELECT user_id, user_name, email " +
                        "FROM users " +
                        "WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("User not found!");
                }
                long id = rs.getInt   ("user_id");
                String fullName = rs.getString("user_name");
                String mail = rs.getString("email");

                return new User(id, fullName, mail);
            }
        }
    }

    //загрузка пароля из БД
    private String getUserHashedPW(String email) throws IncorrectPasswordException, SQLException {
        String sql = "SELECT password_hash FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new IncorrectPasswordException("User not found!");

                return resultSet.getString("password_hash");
            }
        }
    }

    //регистрация пользователя
    public String register(String fullName, String password, String email) throws RegistrationInputException, SQLException {
        //проверки на валидность введенного пароля/доступность логина
        checkPasswordValidity(password);
        checkNameValidity(fullName);
        checkEmailExistence(email);

        //хэшируем пароль и добавляем юзера в БД
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        insertUserIntoDB(fullName, hashedPassword, email);
        return JWTHandler.createUserToken(loadUser(email));
    }

    //добавление юзера в БД
    private void insertUserIntoDB(String fullName, String password, String email) throws SQLException {
        String sql = "INSERT INTO users (user_name, password_hash, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameters in the PreparedStatement
            pstmt.setString(1, fullName);   // Set username
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

    private void checkEmailExistence(String email) throws RegistrationInputException, SQLException {
        String sql =
                "SELECT " +
                        "  EXISTS(SELECT 1 FROM users WHERE email = ?) AS email_exists";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean emailExists = rs.getBoolean("email_exists");

                    if (emailExists) {
                        throw new RegistrationInputException("Either Username or Email already exists!");
                    }
                }
            }
        }
    }

    private void checkNameValidity(String name) throws RegistrationInputException {
        if (name.length() < 1)
            throw new RegistrationInputException("ФИО не указано.");
        // другие проверки
        // ...
    }
}
