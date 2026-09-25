package mehedi.stockwatch.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interaso.webpush.WebPush;
import com.interaso.webpush.WebPushService;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Primary
public class WebPushNotificationService
        implements NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WebPushNotificationService.class
            );

    private final PushSubscriptionRepository repository;
    private final StockRepository stockRepository;
    private final WebPushService webPushService;
    private final ObjectMapper objectMapper;

    public WebPushNotificationService(
            PushSubscriptionRepository repository,
            StockRepository stockRepository,
            WebPushService webPushService,
            ObjectMapper objectMapper
    ) {

        this.repository = repository;
        this.stockRepository = stockRepository;
        this.webPushService = webPushService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendStockAlert(
            StockResponse stock,
            BigDecimal currentPrice
    ) {

        Optional<Long> ownerUserId =
                stockRepository
                        .findOwnerUserIdByStockId(
                                stock.id()
                        );

        /*
         * Old stocks may temporarily have
         * user_id = NULL.
         */
        if (ownerUserId.isEmpty()) {

            log.warn(
                    "Skipping push alert for stock {} because it has no owner",
                    stock.id()
            );

            return;
        }

        List<PushSubscription> subscriptions =
                repository.findAllByUser_Id(
                        ownerUserId.get()
                );

        if (subscriptions.isEmpty()) {

            log.debug(
                    "No push subscriptions for user {}",
                    ownerUserId.get()
            );

            return;
        }

        String payload;

        try {

            payload =
                    objectMapper.writeValueAsString(
                            new NotificationPayload(
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
                            )
                    );

        } catch (Exception exception) {

            log.error(
                    "Could not create push payload for {}",
                    stock.ticker(),
                    exception
            );

            return;
        }

        for (
                PushSubscription subscription
                : subscriptions
        ) {

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

                if (
                        state
                                ==
                                WebPush.SubscriptionState.EXPIRED
                ) {

                    log.info(
                            "Deleting expired push subscription {}",
                            subscription.getId()
                    );

                    repository.delete(
                            subscription
                    );
                }

            } catch (Exception exception) {

                log.warn(
                        "Failed to send push notification to subscription {}",
                        subscription.getId(),
                        exception
                );
            }
        }
    }

    private record NotificationPayload(
            String title,
            String body,
            String ticker,
            BigDecimal currentPrice,
            BigDecimal targetPrice
    ) {
    }
}