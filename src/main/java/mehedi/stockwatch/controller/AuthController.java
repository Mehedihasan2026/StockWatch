package mehedi.stockwatch.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mehedi.stockwatch.dto.AuthUserResponse;
import mehedi.stockwatch.dto.LoginRequest;
import mehedi.stockwatch.dto.RegisterRequest;
import mehedi.stockwatch.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager
            authenticationManager;
    private final SecurityContextRepository
            securityContextRepository;
    private final SessionAuthenticationStrategy
            sessionAuthenticationStrategy;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy
                    sessionAuthenticationStrategy) {

        this.authService = authService;
        this.authenticationManager =
                authenticationManager;

        this.securityContextRepository =
                securityContextRepository;

        this.sessionAuthenticationStrategy =
                sessionAuthenticationStrategy;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(
            CsrfToken csrfToken) {

        return csrfToken;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse register(
            @Valid
            @RequestBody
            RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthUserResponse login(
            @Valid
            @RequestBody
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        try {

            Authentication authenticationRequest =
                    UsernamePasswordAuthenticationToken
                            .unauthenticated(
                                    request.email()
                                            .trim()
                                            .toLowerCase(),
                                    request.password()
                            );

            Authentication authentication =
                    authenticationManager
                            .authenticate(
                                    authenticationRequest
                            );

            sessionAuthenticationStrategy
                    .onAuthentication(
                            authentication,
                            httpRequest,
                            httpResponse
                    );

            SecurityContext context =
                    SecurityContextHolder
                            .createEmptyContext();

            context.setAuthentication(
                    authentication
            );

            SecurityContextHolder.setContext(
                    context
            );

            securityContextRepository
                    .saveContext(
                            context,
                            httpRequest,
                            httpResponse
                    );

            return authService.getByEmail(
                    authentication.getName()
            );

        } catch (AuthenticationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }
    }

    @GetMapping("/me")
    public AuthUserResponse me(
            Authentication authentication) {

        return authService.getByEmail(
                authentication.getName()
        );
    }
}