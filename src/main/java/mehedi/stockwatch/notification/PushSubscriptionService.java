package mehedi.stockwatch.notification;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PushSubscriptionService {

    private final PushSubscriptionRepository repository;

    public PushSubscriptionService(
            PushSubscriptionRepository repository) {

        this.repository = repository;
    }

    public void saveSubscription(
            PushSubscriptionRequest request) {

        LocalDateTime now = LocalDateTime.now();

        PushSubscription subscription =
                repository.findByEndpoint(request.endpoint())
                        .orElseGet(() -> {
                            PushSubscription newSubscription =
                                    new PushSubscription();

                            newSubscription.setEndpoint(
                                    request.endpoint()
                            );

                            newSubscription.setCreatedAt(now);

                            return newSubscription;
                        });

        subscription.setP256dh(request.p256dh());
        subscription.setAuth(request.auth());
        subscription.setUpdatedAt(now);

        repository.save(subscription);
    }
    public void deleteSubscription(String endpoint) {

        repository.findByEndpoint(endpoint)
                .ifPresent(repository::delete);
    }
}