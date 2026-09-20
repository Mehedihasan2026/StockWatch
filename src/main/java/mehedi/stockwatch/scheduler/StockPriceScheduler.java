package mehedi.stockwatch.scheduler;

import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.market.MarketDataException;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.notification.NotificationService;
import mehedi.stockwatch.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StockPriceScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(StockPriceScheduler.class);

    private final StockService stockService;
    private final MarketDataService marketDataService;
    private final NotificationService notificationService;

    public StockPriceScheduler(
            StockService stockService,
            MarketDataService marketDataService,
            NotificationService notificationService) {

        this.stockService = stockService;
        this.marketDataService = marketDataService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkStockPrices() {

        List<StockResponse> stocks =
                stockService.getStocksForMonitoring();

        for (StockResponse stock : stocks) {

            try {

                BigDecimal currentPrice =
                        marketDataService.getCurrentPrice(
                                stock.ticker()
                        );

                log.info(
                        "{} current price: {}",
                        stock.ticker(),
                        currentPrice
                );

                boolean alertTriggered =
                        stockService.checkAndUpdateAlert(
                                stock.id(),
                                currentPrice
                        );

                if (alertTriggered) {
                    notificationService.sendStockAlert(
                            stock,
                            currentPrice
                    );
                }

            } catch (MarketDataException exception) {

                log.error(
                        "Failed to retrieve market data for {}: {}",
                        stock.ticker(),
                        exception.getMessage()
                );
            }
        }
    }
}