package authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

public class JWTHandler {
    public static User getUser(String token) throws JwtException {
        //парсим токен авторизации
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(AuthorizationHandler.key)
                .build()
                .parseSignedClaims(token);
        //создаем и возвращаем пользователя с данными согласно токену
        return new User(Long.valueOf(claims.getPayload().getId()),
                claims.getPayload().getSubject(),
                claims.getPayload().get("email", String.class));
    }
}
