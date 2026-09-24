package mehedi.stockwatch.service;

import mehedi.stockwatch.dto.AuthUserResponse;
import mehedi.stockwatch.dto.RegisterRequest;
import mehedi.stockwatch.entity.User;
import mehedi.stockwatch.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUserResponse register(
            RegisterRequest request) {

        String email =
                normalizeEmail(request.email());

        if (userRepository
                .existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists."
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        User user = new User();

        user.setEmail(email);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setDisplayName(
                request.displayName().trim()
        );

        user.setEnabled(true);

        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return toResponse(
                userRepository.save(user)
        );
    }

    public AuthUserResponse getByEmail(
            String email) {

        User user =
                userRepository
                        .findByEmailIgnoreCase(
                                normalizeEmail(email)
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found."
                                )
                        );

        return toResponse(user);
    }

    private String normalizeEmail(
            String email) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private AuthUserResponse toResponse(
            User user) {

        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }
}