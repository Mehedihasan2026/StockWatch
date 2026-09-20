package mehedi.stockwatch.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interaso.webpush.WebPushService;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.entity.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.interaso.webpush.WebPush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

class WebPushNotificationServiceTest {

    private PushSubscriptionRepository repository;
    private WebPushService webPushService;
    private WebPushNotificationService service;

    @BeforeEach
    void setUp() {

        repository = mock(PushSubscriptionRepository.class);
        webPushService = mock(WebPushService.class);

        ObjectMapper objectMapper =
                new ObjectMapper();

        service = new WebPushNotificationService(
                repository,
                webPushService,
                objectMapper
        );
    }

    @Test
    void shouldNotSendPush_whenThereAreNoSubscriptions() {

        when(repository.findAll())
                .thenReturn(List.of());

        StockResponse stock = createStock();

        service.sendStockAlert(
                stock,
                new BigDecimal("1100.00")
        );

        verify(repository).findAll();

        verifyNoInteractions(webPushService);
    }
    @Test
    void shouldSendPush_whenSubscriptionIsActive() {

        PushSubscription subscription =
                new PushSubscription();

        subscription.setId(1L);
        subscription.setEndpoint(
                "https://push.example/device123"
        );
        subscription.setP256dh(
                "test-public-key"
        );
        subscription.setAuth(
                "test-auth-key"
        );

        when(repository.findAll())
                .thenReturn(List.of(subscription));

        when(webPushService.send(
                anyString(),
                eq("https://push.example/device123"),
                eq("test-public-key"),
                eq("test-auth-key"),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(
                WebPush.SubscriptionState.ACTIVE
        );

        StockResponse stock = createStock();

        service.sendStockAlert(
                stock,
                new BigDecimal("1100.00")
        );

        verify(webPushService).send(
                anyString(),
                eq("https://push.example/device123"),
                eq("test-public-key"),
                eq("test-auth-key"),
                isNull(),
                isNull(),
                isNull()
        );

        verify(repository, never())
                .delete(subscription);
    }
    @Test
    void shouldDeleteSubscription_whenSubscriptionIsExpired() {

        PushSubscription subscription =
                new PushSubscription();

        subscription.setId(1L);
        subscription.setEndpoint(
                "https://push.example/device123"
        );
        subscription.setP256dh(
                "test-public-key"
        );
        subscription.setAuth(
                "test-auth-key"
        );

        when(repository.findAll())
                .thenReturn(List.of(subscription));

        when(webPushService.send(
                anyString(),
                eq("https://push.example/device123"),
                eq("test-public-key"),
                eq("test-auth-key"),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(
                WebPush.SubscriptionState.EXPIRED
        );

        service.sendStockAlert(
                createStock(),
                new BigDecimal("1100.00")
        );

        verify(repository).delete(subscription);
    }
    @Test
    void shouldSendExpectedJsonPayload() throws Exception {

        PushSubscription subscription =
                new PushSubscription();

        subscription.setId(1L);
        subscription.setEndpoint(
                "https://push.example/device123"
        );
        subscription.setP256dh(
                "test-public-key"
        );
        subscription.setAuth(
                "test-auth-key"
        );

        when(repository.findAll())
                .thenReturn(List.of(subscription));

        when(webPushService.send(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(
                WebPush.SubscriptionState.ACTIVE
        );

        service.sendStockAlert(
                createStock(),
                new BigDecimal("1100.00")
        );

        var payloadCaptor =
                org.mockito.ArgumentCaptor
                        .forClass(String.class);

        verify(webPushService).send(
                payloadCaptor.capture(),
                eq("https://push.example/device123"),
                eq("test-public-key"),
                eq("test-auth-key"),
                isNull(),
                isNull(),
                isNull()
        );

        String payload =
                payloadCaptor.getValue();

        ObjectMapper objectMapper =
                new ObjectMapper();

        var json =
                objectMapper.readTree(payload);

        assertEquals(
                "StockWatch: MU alert",
                json.get("title").asText()
        );

        assertEquals(
                "MU",
                json.get("ticker").asText()
        );

        assertEquals(
                1100.00,
                json.get("currentPrice").asDouble()
        );

        assertEquals(
                1096.00,
                json.get("targetPrice").asDouble()
        );

        assertTrue(
                json.get("body")
                        .asText()
                        .contains("1100.00")
        );
    }

    private StockResponse createStock() {

        return new StockResponse(
                1L,
                "MU",
                "Micron Technology, Inc.",
                5,
                new BigDecimal("1015.60"),
                Currency.USD,
                new BigDecimal("1096.00"),
                "Testing alert",
                true,
                true,
                new BigDecimal("1096.00"),
                null,
                null
        );
    }
}