package mehedi.stockwatch.scheduler;

import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.entity.Currency;
import mehedi.stockwatch.market.MarketDataException;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

class StockPriceSchedulerTest {

    private StockService stockService;
    private MarketDataService marketDataService;
    private StockPriceScheduler scheduler;

    @BeforeEach
    void setUp() {

        stockService = mock(StockService.class);
        marketDataService = mock(MarketDataService.class);

        scheduler = new StockPriceScheduler(
                stockService,
                marketDataService
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
}
