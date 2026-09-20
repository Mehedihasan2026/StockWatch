package mehedi.stockwatch.notification;

import mehedi.stockwatch.dto.StockResponse;

import java.math.BigDecimal;

public interface NotificationService {

    void sendStockAlert(
            StockResponse stock,
            BigDecimal currentPrice
    );
}