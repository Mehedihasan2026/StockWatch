package mehedi.stockwatch.notification;

import mehedi.stockwatch.entity.User;
import mehedi.stockwatch.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class PushSubscriptionService {

    private final PushSubscriptionRepository repository;
    private final UserRepository userRepository;

    public PushSubscriptionService(
            PushSubscriptionRepository repository,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void saveSubscription(
            PushSubscriptionRequest request,
            String userEmail
    ) {

        User user = getUser(userEmail);

        LocalDateTime now =
                LocalDateTime.now();

        PushSubscription subscription =
                repository
                        .findByEndpoint(
                                request.endpoint()
                        )
                        .orElseGet(() -> {

                            PushSubscription created =
                                    new PushSubscription();

                            created.setEndpoint(
                                    request.endpoint()
                            );

                            created.setCreatedAt(now);

                            return created;
                        });

        /*
         * If the same browser is later used by
         * another StockWatch account, ownership
         * of that browser push endpoint follows
         * the currently authenticated account.
         */
        subscription.setUser(user);

        subscription.setP256dh(
                request.p256dh()
        );

        subscription.setAuth(
                request.auth()
        );

        subscription.setUpdatedAt(now);

        repository.save(subscription);
    }

    @Transactional
    public void deleteSubscription(
            String endpoint,
            String userEmail
    ) {

        User user = getUser(userEmail);

        repository.deleteByEndpointAndUser_Id(
                endpoint,
                user.getId()
        );
    }

    private User getUser(
            String email
    ) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found."
                        )
                );
    }
}