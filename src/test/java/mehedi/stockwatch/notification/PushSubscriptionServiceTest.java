package mehedi.stockwatch.notification;

import mehedi.stockwatch.entity.User;
import mehedi.stockwatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushSubscriptionServiceTest {

    private static final String EMAIL =
            "hasan@example.com";

    private static final Long USER_ID =
            42L;

    private PushSubscriptionRepository repository;

    private UserRepository userRepository;

    private PushSubscriptionService service;

    private User user;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        PushSubscriptionRepository.class
                );

        userRepository =
                mock(
                        UserRepository.class
                );

        service =
                new PushSubscriptionService(
                        repository,
                        userRepository
                );


        user =
                new User();

        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setDisplayName("Hasan");
        user.setEnabled(true);


        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        ).thenReturn(
                Optional.of(user)
        );
    }


    @Test
    void shouldCreateNewSubscriptionForUser() {

        PushSubscriptionRequest request =
                new PushSubscriptionRequest(
                        "https://push.example.com/123",
                        "test-p256dh",
                        "test-auth"
                );


        when(
                repository.findByEndpoint(
                        request.endpoint()
                )
        ).thenReturn(
                Optional.empty()
        );


        service.saveSubscription(
                request,
                EMAIL
        );


        ArgumentCaptor<PushSubscription> captor =
                ArgumentCaptor.forClass(
                        PushSubscription.class
                );


        verify(repository)
                .save(
                        captor.capture()
                );


        PushSubscription saved =
                captor.getValue();


        assertEquals(
                request.endpoint(),
                saved.getEndpoint()
        );

        assertEquals(
                request.p256dh(),
                saved.getP256dh()
        );

        assertEquals(
                request.auth(),
                saved.getAuth()
        );

        assertSame(
                user,
                saved.getUser()
        );

        assertNotNull(
                saved.getCreatedAt()
        );

        assertNotNull(
                saved.getUpdatedAt()
        );
    }


    @Test
    void shouldUpdateExistingSubscriptionAndAssignCurrentUser() {

        String endpoint =
                "https://push.example.com/123";


        PushSubscription existing =
                new PushSubscription();

        existing.setId(10L);
        existing.setEndpoint(endpoint);


        PushSubscriptionRequest request =
                new PushSubscriptionRequest(
                        endpoint,
                        "new-p256dh",
                        "new-auth"
                );


        when(
                repository.findByEndpoint(
                        endpoint
                )
        ).thenReturn(
                Optional.of(existing)
        );


        service.saveSubscription(
                request,
                EMAIL
        );


        assertSame(
                user,
                existing.getUser()
        );

        assertEquals(
                "new-p256dh",
                existing.getP256dh()
        );

        assertEquals(
                "new-auth",
                existing.getAuth()
        );

        assertNotNull(
                existing.getUpdatedAt()
        );


        verify(repository)
                .save(existing);
    }


    @Test
    void shouldDeleteOnlySubscriptionOwnedByUser() {

        String endpoint =
                "https://push.example.com/123";


        service.deleteSubscription(
                endpoint,
                EMAIL
        );


        verify(repository)
                .deleteByEndpointAndUser_Id(
                        endpoint,
                        USER_ID
                );
    }


    @Test
    void shouldFailWhenAuthenticatedUserDoesNotExist() {

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                "missing@example.com"
                        )
        ).thenReturn(
                Optional.empty()
        );


        PushSubscriptionRequest request =
                new PushSubscriptionRequest(
                        "https://push.example.com/123",
                        "p256dh",
                        "auth"
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.saveSubscription(
                                        request,
                                        "missing@example.com"
                                )
                );


        assertEquals(
                401,
                exception
                        .getStatusCode()
                        .value()
        );


        verify(
                repository,
                never()
        ).save(any());
    }
}