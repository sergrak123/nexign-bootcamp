package ru.grak.crm.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.grak.crm.dto.auth.JwtResponse;
import ru.grak.crm.dto.auth.LoginRequest;
import ru.grak.crm.entity.User;
import ru.grak.crm.security.jwt.TokenProvider;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserService userService;

    /**
     * Метод для аутентификации пользователя.
     *
     * @param loginRequest Данные пользователя для входа.
     * @return Ответ с JWT-токеном и другими данными пользователя.
     */
    public JwtResponse login(LoginRequest loginRequest) {

        String username = loginRequest.getUsername();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));
        User users = userService.findByUsername(username).get();
        String token = tokenProvider.createToken(username, users.getRoles());

        return JwtResponse.builder()
                .id(users.getId())
                .token(token)
                .username(username)
                .roles(users.getRoles())
                .build();
    }
}
