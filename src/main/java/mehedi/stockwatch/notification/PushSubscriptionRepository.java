package mehedi.stockwatch.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository
        extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByEndpoint(
            String endpoint
    );

    Optional<PushSubscription> findByEndpointAndUser_Id(
            String endpoint,
            Long userId
    );

    List<PushSubscription> findAllByUser_Id(
            Long userId
    );

    long deleteByEndpointAndUser_Id(
            String endpoint,
            Long userId
    );
}