package mehedi.stockwatch.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

class PushSubscriptionControllerTest {

    private PushSubscriptionService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        service = mock(PushSubscriptionService.class);

        PushSubscriptionController controller =
                new PushSubscriptionController(service);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturn204_whenSubscriptionIsValid()
            throws Exception {

        String requestBody = """
                {
                  "endpoint": "https://push.example/device123",
                  "p256dh": "public-key",
                  "auth": "auth-key"
                }
                """;

        mockMvc.perform(
                        post("/api/push-subscriptions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isNoContent());

        verify(service).saveSubscription(any());
    }

    @Test
    void shouldReturn400_whenEndpointIsMissing()
            throws Exception {

        String requestBody = """
                {
                  "p256dh": "public-key",
                  "auth": "auth-key"
                }
                """;

        mockMvc.perform(
                        post("/api/push-subscriptions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(service, never())
                .saveSubscription(any());
    }

    @Test
    void shouldReturn400_whenP256dhIsBlank()
            throws Exception {

        String requestBody = """
                {
                  "endpoint": "https://push.example/device123",
                  "p256dh": "",
                  "auth": "auth-key"
                }
                """;

        mockMvc.perform(
                        post("/api/push-subscriptions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(service, never())
                .saveSubscription(any());
    }

    @Test
    void shouldReturn400_whenAuthIsMissing()
            throws Exception {

        String requestBody = """
                {
                  "endpoint": "https://push.example/device123",
                  "p256dh": "public-key"
                }
                """;

        mockMvc.perform(
                        post("/api/push-subscriptions")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(service, never())
                .saveSubscription(any());
    }
    @Test
    void shouldReturn204_whenSubscriptionIsDeleted()
            throws Exception {

        mockMvc.perform(
                        delete("/api/push-subscriptions")
                                .param(
                                        "endpoint",
                                        "https://push.example/device123"
                                )
                )
                .andExpect(status().isNoContent());

        verify(service).deleteSubscription(
                "https://push.example/device123"
        );
    }
}