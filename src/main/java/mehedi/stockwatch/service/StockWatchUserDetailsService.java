package mehedi.stockwatch.service;

import mehedi.stockwatch.entity.User;
import mehedi.stockwatch.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StockWatchUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public StockWatchUserDetailsService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String email)
            throws UsernameNotFoundException {

        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found."
                                )
                        );

        return org.springframework.security
                .core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .disabled(
                        !Boolean.TRUE.equals(
                                user.getEnabled()
                        )
                )
                .build();
    }
}