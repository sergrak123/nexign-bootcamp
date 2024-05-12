package ru.grak.crm.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.grak.crm.entity.Role;
import ru.grak.crm.security.UserDetailsServiceImpl;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для создания, валидации и извлечения JWT-токенов.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

//    @Value("${jwt.token.secret}")
//    private String secret;

    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Value("${jwt.token.expired}")
    private long validityInMilliseconds;

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Метод для создания JWT-токена на основе имени пользователя и его ролей.
     *
     * @param username имя пользователя
     * @param roles    список ролей пользователя
     * @return JWT-токен
     */
    public String createToken(String username, List<Role> roles) {

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", getRoleNames(roles));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Метод для извлечения JWT-токена из запроса.
     *
     * @param req HTTP-запрос
     * @return JWT-токен
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Метод для проверки валидности JWT-токена.
     *
     * @param token JWT-токен
     * @return true, если токен валиден, иначе - false
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException("JWT token is expired or invalid");
        }
    }

    /**
     * Метод для извлечения имени пользователя из JWT-токена.
     *
     * @param token JWT-токен
     * @return имя пользователя
     */
    public String getUsername(String token) {

        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    private List<String> getRoleNames(List<Role> userRoles) {
        return userRoles.stream()
                .map(role -> role.getName().getAuthority()).collect(Collectors.toList());
    }

    /**
     * Метод для получения аутентификации пользователя на основе JWT-токена.
     *
     * @param token JWT-токен
     * @return аутентификация пользователя
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
