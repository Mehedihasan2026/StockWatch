package mehedi.stockwatch.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PushSubscriptionServiceTest {

    private PushSubscriptionRepository repository;
    private PushSubscriptionService service;

    @BeforeEach
    void setUp() {
        repository = mock(PushSubscriptionRepository.class);
        service = new PushSubscriptionService(repository);
    }

    @Test
    void shouldCreateNewSubscription_whenEndpointDoesNotExist() {

        PushSubscriptionRequest request =
                new PushSubscriptionRequest(
                        "https://push.example/test",
                        "public-key",
                        "auth-key"
                );

        when(repository.findByEndpoint(request.endpoint()))
                .thenReturn(Optional.empty());

        service.saveSubscription(request);

        verify(repository).save(argThat(subscription ->
                subscription.getId() == null
                        && subscription.getEndpoint()
                        .equals("https://push.example/test")
                        && subscription.getP256dh()
                        .equals("public-key")
                        && subscription.getAuth()
                        .equals("auth-key")
                        && subscription.getCreatedAt() != null
                        && subscription.getUpdatedAt() != null
        ));
    }

    @Test
    void shouldUpdateExistingSubscription_whenEndpointAlreadyExists() {

        PushSubscription existing =
                new PushSubscription();

        existing.setId(1L);
        existing.setEndpoint(
                "https://push.example/test"
        );
        existing.setP256dh("old-public-key");
        existing.setAuth("old-auth-key");

        PushSubscriptionRequest request =
                new PushSubscriptionRequest(
                        "https://push.example/test",
                        "new-public-key",
                        "new-auth-key"
                );

        when(repository.findByEndpoint(request.endpoint()))
                .thenReturn(Optional.of(existing));

        service.saveSubscription(request);

        assertEquals(
                "new-public-key",
                existing.getP256dh()
        );

        assertEquals(
                "new-auth-key",
                existing.getAuth()
        );

        verify(repository).save(existing);
    }
    @Test
    void shouldDeleteSubscription_whenEndpointExists() {

        PushSubscription subscription =
                new PushSubscription();

        subscription.setId(1L);
        subscription.setEndpoint(
                "https://push.example/device123"
        );

        when(repository.findByEndpoint(
                "https://push.example/device123"
        )).thenReturn(
                Optional.of(subscription)
        );

        service.deleteSubscription(
                "https://push.example/device123"
        );

        verify(repository).delete(subscription);
    }

    @Test
    void shouldDoNothing_whenDeletingUnknownEndpoint() {

        when(repository.findByEndpoint(
                "https://push.example/unknown"
        )).thenReturn(
                Optional.empty()
        );

        service.deleteSubscription(
                "https://push.example/unknown"
        );

        verify(repository, never())
                .delete(any());
    }
}