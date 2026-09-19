package mehedi.stockwatch.notification;

import mehedi.stockwatch.config.VapidProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PushConfigControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        VapidProperties vapidProperties =
                new VapidProperties();

        vapidProperties.setVapidPublicKey(
                "test-public-key"
        );

        vapidProperties.setVapidPrivateKey(
                "secret-private-key"
        );

        vapidProperties.setVapidSubject(
                "mailto:test@example.com"
        );

        PushConfigController controller =
                new PushConfigController(vapidProperties);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldReturnOnlyVapidPublicKey()
            throws Exception {

        mockMvc.perform(
                        get("/api/push/public-key")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.publicKey")
                                .value("test-public-key")
                )
                .andExpect(
                        jsonPath("$.vapidPrivateKey")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.vapidSubject")
                                .doesNotExist()
                );
    }
}