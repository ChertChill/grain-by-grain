package authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTHandler {
    //секретный ключ, необходимый для подписи/верификации JWT-токенов
    //по хорошему, вынести в отдельный файл конфигурации
    static SecretKey key = Jwts.SIG.HS256.key().build();
    static final int tokenExpirationTime = 60;

    public static User getUser(String token) throws JwtException {
        //парсим токен авторизации
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        //создаем и возвращаем пользователя с данными согласно токену
        return new User(Long.valueOf(claims.getPayload().getId()),
                claims.getPayload().getSubject(),
                claims.getPayload().get("email", String.class));
    }

    public static String createUserToken(User user) {
        long now = System.currentTimeMillis();
        Date expirationDate = new Date(now + (tokenExpirationTime*60*1000));

        //создаем и возвращаем токен авторизации
        return Jwts.builder()
                .subject(user.getFullName())
                .id(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .expiration(expirationDate)
                .signWith(key).compact();
    }
}
