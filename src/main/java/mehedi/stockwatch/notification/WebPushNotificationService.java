package mehedi.stockwatch.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interaso.webpush.WebPush;
import com.interaso.webpush.WebPushService;
import mehedi.stockwatch.dto.StockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Primary
public class WebPushNotificationService
        implements NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WebPushNotificationService.class
            );

    private final PushSubscriptionRepository repository;
    private final WebPushService webPushService;
    private final ObjectMapper objectMapper;

    public WebPushNotificationService(
            PushSubscriptionRepository repository,
            WebPushService webPushService,
            ObjectMapper objectMapper) {

        this.repository = repository;
        this.webPushService = webPushService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendStockAlert(
            StockResponse stock,
            BigDecimal currentPrice) {

        List<PushSubscription> subscriptions =
                repository.findAll();

        if (subscriptions.isEmpty()) {

            log.info(
                    "No push subscriptions available for {} alert",
                    stock.ticker()
            );

            return;
        }

        String payload =
                createPayload(stock, currentPrice);

        for (PushSubscription subscription : subscriptions) {

            try {

                WebPush.SubscriptionState state =
                        webPushService.send(
                                payload,
                                subscription.getEndpoint(),
                                subscription.getP256dh(),
                                subscription.getAuth(),
                                null,
                                null,
                                null
                        );

                if (state == WebPush.SubscriptionState.EXPIRED) {

                    repository.delete(subscription);

                    log.info(
                            "Removed expired push subscription: {}",
                            subscription.getId()
                    );

                } else {

                    log.info(
                            "Push notification sent for {}",
                            stock.ticker()
                    );
                }

            } catch (Exception exception) {

                log.error(
                        "Failed to send push notification for {}: {}",
                        stock.ticker(),
                        exception.getMessage()
                );
            }
        }
    }

    private String createPayload(
            StockResponse stock,
            BigDecimal currentPrice) {

        StockAlertPayload payload =
                new StockAlertPayload(
                        "StockWatch: "
                                + stock.ticker()
                                + " alert",

                        stock.ticker()
                                + " is now "
                                + currentPrice
                                + " "
                                + stock.currency()
                                + " (target "
                                + stock.targetPrice()
                                + ")",

                        stock.ticker(),
                        currentPrice,
                        stock.targetPrice()
                );

        try {

            return objectMapper.writeValueAsString(
                    payload
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Failed to create push notification payload",
                    exception
            );
        }
    }

    private record StockAlertPayload(
            String title,
            String body,
            String ticker,
            BigDecimal currentPrice,
            BigDecimal targetPrice
    ) {
    }
}