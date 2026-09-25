package mehedi.stockwatch.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PushSubscriptionControllerTest {

    private static final String EMAIL =
            "hasan@example.com";

    private PushSubscriptionService service;

    private MockMvc mockMvc;

    private Authentication authentication;


    @BeforeEach
    void setUp() {

        service =
                mock(
                        PushSubscriptionService.class
                );

        PushSubscriptionController controller =
                new PushSubscriptionController(
                        service
                );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                controller
                        )
                        .setValidator(
                                validator
                        )
                        .build();

        authentication =
                new UsernamePasswordAuthenticationToken(
                        EMAIL,
                        "password",
                        java.util.List.of()
                );
    }


    @Test
    void shouldSaveValidSubscription()
            throws Exception {

        String body = """
                {
                    "endpoint": "https://push.example.com/123",
                    "p256dh": "test-p256dh",
                    "auth": "test-auth"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/push-subscriptions"
                        )
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(body)
                )
                .andExpect(
                        status()
                                .isNoContent()
                );


        verify(service)
                .saveSubscription(
                        any(
                                PushSubscriptionRequest.class
                        ),
                        eq(EMAIL)
                );
    }


    @Test
    void shouldRejectMissingEndpoint()
            throws Exception {

        String body = """
                {
                    "endpoint": "",
                    "p256dh": "test-p256dh",
                    "auth": "test-auth"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/push-subscriptions"
                        )
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(body)
                )
                .andExpect(
                        status()
                                .isBadRequest()
                );


        verify(
                service,
                never()
        ).saveSubscription(
                any(),
                any()
        );
    }


    @Test
    void shouldRejectMissingP256dh()
            throws Exception {

        String body = """
                {
                    "endpoint": "https://push.example.com/123",
                    "p256dh": "",
                    "auth": "test-auth"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/push-subscriptions"
                        )
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(body)
                )
                .andExpect(
                        status()
                                .isBadRequest()
                );


        verify(
                service,
                never()
        ).saveSubscription(
                any(),
                any()
        );
    }


    @Test
    void shouldRejectMissingAuth()
            throws Exception {

        String body = """
                {
                    "endpoint": "https://push.example.com/123",
                    "p256dh": "test-p256dh",
                    "auth": ""
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/push-subscriptions"
                        )
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(body)
                )
                .andExpect(
                        status()
                                .isBadRequest()
                );


        verify(
                service,
                never()
        ).saveSubscription(
                any(),
                any()
        );
    }


    @Test
    void shouldDeleteSubscriptionForAuthenticatedUser()
            throws Exception {

        String endpoint =
                "https://push.example.com/123";


        mockMvc.perform(
                        delete(
                                "/api/push-subscriptions"
                        )
                                .principal(
                                        authentication
                                )
                                .param(
                                        "endpoint",
                                        endpoint
                                )
                )
                .andExpect(
                        status()
                                .isNoContent()
                );


        verify(service)
                .deleteSubscription(
                        endpoint,
                        EMAIL
                );
    }
}