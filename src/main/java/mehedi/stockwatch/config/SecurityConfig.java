package mehedi.stockwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return new ProviderManager(
                provider
        );
    }


    @Bean
    public SecurityContextRepository
    securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }


    @Bean
    public SessionAuthenticationStrategy
    sessionAuthenticationStrategy() {

        return new ChangeSessionIdAuthenticationStrategy();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository
    ) throws Exception {

        /*
         * We intentionally use the session-based
         * CSRF repository because React retrieves
         * the token explicitly from /api/auth/csrf.
         */
        HttpSessionCsrfTokenRepository csrfRepository =
                new HttpSessionCsrfTokenRepository();

        csrfRepository.setHeaderName(
                "X-CSRF-TOKEN"
        );


        /*
         * Use the raw token in the request header.
         * This matches what our React api.ts sends.
         */
        CsrfTokenRequestAttributeHandler csrfHandler =
                new CsrfTokenRequestAttributeHandler();

        csrfHandler.setCsrfRequestAttributeName(
                null
        );


        http
                .cors(
                        Customizer.withDefaults()
                )

                .csrf(csrf ->
                        csrf
                                .csrfTokenRepository(
                                        csrfRepository
                                )
                                .csrfTokenRequestHandler(
                                        csrfHandler
                                )
                )

                .securityContext(context ->
                        context
                                .securityContextRepository(
                                        securityContextRepository
                                )
                )

                .authorizeHttpRequests(auth ->
                        auth

                                /*
                                 * Public authentication
                                 * endpoints.
                                 */
                                .requestMatchers(
                                        "/api/auth/register",
                                        "/api/auth/login",
                                        "/api/auth/csrf"
                                )
                                .permitAll()

                                /*
                                 * Public market page.
                                 */
                                .requestMatchers(
                                        "/api/market/**"
                                )
                                .permitAll()

                                /*
                                 * Public VAPID key.
                                 *
                                 * This is safe to expose.
                                 */
                                .requestMatchers(
                                        "/api/push/public-key"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/error"
                                )
                                .permitAll()

                                /*
                                 * Everything else requires
                                 * authentication.
                                 */
                                .anyRequest()
                                .authenticated()
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        (
                                                request,
                                                response,
                                                authException
                                        ) ->
                                                response.sendError(
                                                        401
                                                )
                                )
                )

                .logout(logout ->
                        logout
                                .logoutUrl(
                                        "/api/auth/logout"
                                )
                                .logoutSuccessHandler(
                                        (
                                                request,
                                                response,
                                                authentication
                                        ) ->
                                                response.setStatus(
                                                        204
                                                )
                                )
                                .invalidateHttpSession(
                                        true
                                )
                                .deleteCookies(
                                        "JSESSIONID"
                                )
                );


        return http.build();
    }
}