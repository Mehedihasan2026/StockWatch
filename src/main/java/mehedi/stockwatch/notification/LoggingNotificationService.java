package mehedi.stockwatch.notification;

import mehedi.stockwatch.dto.StockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LoggingNotificationService
        implements NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    LoggingNotificationService.class
            );

    @Override
    public void sendStockAlert(
            StockResponse stock,
            BigDecimal currentPrice) {

        log.info(
                "{} ALERT! Current price: {}, target: {}",
                stock.ticker(),
                currentPrice,
                stock.targetPrice()
        );
    }
}