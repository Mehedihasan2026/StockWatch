package mehedi.stockwatch.scheduler;

import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.entity.Currency;
import mehedi.stockwatch.market.MarketDataException;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mehedi.stockwatch.notification.NotificationService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

class StockPriceSchedulerTest {

    private StockService stockService;
    private MarketDataService marketDataService;
    private StockPriceScheduler scheduler;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {

        stockService = mock(StockService.class);
        marketDataService = mock(MarketDataService.class);
        notificationService = mock(NotificationService.class);

        scheduler = new StockPriceScheduler(
                stockService,
                marketDataService,
                notificationService
        );

    }

    @Test
    void shouldContinueMonitoringOtherStocks_whenOneStockFails() {

        StockResponse mu = createStock(1L, "MU");
        StockResponse nvda = createStock(2L, "NVDA");

        when(stockService.getStocksForMonitoring())
                .thenReturn(List.of(mu, nvda));

        when(marketDataService.getCurrentPrice("MU"))
                .thenThrow(new MarketDataException("Yahoo Finance failed"));

        when(marketDataService.getCurrentPrice("NVDA"))
                .thenReturn(new BigDecimal("180.00"));

        scheduler.checkStockPrices();

        verify(marketDataService).getCurrentPrice("MU");
        verify(marketDataService).getCurrentPrice("NVDA");

        verify(stockService).checkAndUpdateAlert(
                2L,
                new BigDecimal("180.00")
        );
    }

    private StockResponse createStock(Long id, String ticker) {

        return new StockResponse(
                id,
                ticker,
                ticker + " Company",
                5,
                new BigDecimal("100.00"),
                Currency.USD,
                new BigDecimal("110.00"),
                "Testing",
                true,
                false,
                null,
                null,
                null
        );
    }
    @Test
    void shouldSendNotification_whenAlertIsTriggered() {

        StockResponse mu = createStock(1L, "MU");

        when(stockService.getStocksForMonitoring())
                .thenReturn(List.of(mu));

        when(marketDataService.getCurrentPrice("MU"))
                .thenReturn(new BigDecimal("120.00"));

        when(stockService.checkAndUpdateAlert(
                1L,
                new BigDecimal("120.00")
        )).thenReturn(true);

        scheduler.checkStockPrices();

        verify(notificationService).sendStockAlert(
                mu,
                new BigDecimal("120.00")
        );
    }
    @Test
    void shouldNotSendNotification_whenAlertIsNotTriggered() {

        StockResponse mu = createStock(1L, "MU");

        when(stockService.getStocksForMonitoring())
                .thenReturn(List.of(mu));

        when(marketDataService.getCurrentPrice("MU"))
                .thenReturn(new BigDecimal("105.00"));

        when(stockService.checkAndUpdateAlert(
                1L,
                new BigDecimal("105.00")
        )).thenReturn(false);

        scheduler.checkStockPrices();

        verify(notificationService, never())
                .sendStockAlert(any(), any());
    }

}
