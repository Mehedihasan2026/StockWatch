package mehedi.stockwatch.service;

import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.entity.Currency;
import mehedi.stockwatch.entity.Stock;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockServiceTest {

    private StockService stockService;
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository = mock(StockRepository.class);
        MarketDataService marketDataService = mock(MarketDataService.class);

        stockService = new StockService(
                stockRepository,
                marketDataService
        );
    }

    @Test
    void shouldTriggerAlert_whenPriceFirstReachesTarget() {

        StockResponse stock = createStock(
                new BigDecimal("1096.00"),
                null
        );

        boolean result = stockService.shouldTriggerAlert(
                stock,
                new BigDecimal("1096.00")
        );

        assertTrue(result);
    }

    @Test
    void shouldNotTriggerAlert_whenPriceIsBelowTarget() {

        StockResponse stock = createStock(
                new BigDecimal("1096.00"),
                null
        );

        boolean result = stockService.shouldTriggerAlert(
                stock,
                new BigDecimal("1095.00")
        );

        assertFalse(result);
    }

    @Test
    void shouldNotTriggerAlert_whenPriceMovementIsLessThanOnePercent() {

        StockResponse stock = createStock(
                new BigDecimal("1096.00"),
                new BigDecimal("1096.00")
        );

        boolean result = stockService.shouldTriggerAlert(
                stock,
                new BigDecimal("1100.00")
        );

        assertFalse(result);
    }

    @Test
    void shouldTriggerAlert_whenPriceMovesUpByAtLeastOnePercent() {

        StockResponse stock = createStock(
                new BigDecimal("1096.00"),
                new BigDecimal("1096.00")
        );

        boolean result = stockService.shouldTriggerAlert(
                stock,
                new BigDecimal("1106.96")
        );

        assertTrue(result);
    }

    @Test
    void shouldNotTriggerAlert_whenPriceDropsLessThanOnePercent() {

        StockResponse stock = createStock(
                new BigDecimal("1096.00"),
                new BigDecimal("1100.00")
        );

        boolean result = stockService.shouldTriggerAlert(
                stock,
                new BigDecimal("1090.00")
        );

        assertFalse(result);
    }

    @Test
    void shouldTriggerAlert_whenPriceDropsByAtLeastOnePercent() {

        StockResponse stock = createStock(
                new BigDecimal("1096.00"),
                new BigDecimal("1110.00")
        );

        boolean result = stockService.shouldTriggerAlert(
                stock,
                new BigDecimal("1099.00")
        );

        assertTrue(result);
    }
    @Test
    void shouldResetAlert_whenPriceFallsBelowTarget() {

        Stock stock = new Stock();
        stock.setId(1L);
        stock.setTicker("MU");
        stock.setTargetPrice(new BigDecimal("100.00"));
        stock.setLastAlertPrice(new BigDecimal("100.00"));
        stock.setAlertTriggered(true);

        when(stockRepository.findById(1L))
                .thenReturn(Optional.of(stock));

        boolean result = stockService.checkAndUpdateAlert(
                1L,
                new BigDecimal("99.00")
        );

        assertFalse(result);
        assertFalse(stock.getAlertTriggered());
        assertNull(stock.getLastAlertPrice());
    }
    @Test
    void shouldTriggerNewAlert_afterPriceFallsBelowTargetAndReachesTargetAgain() {

        Stock stock = new Stock();
        stock.setId(1L);
        stock.setTicker("MU");
        stock.setTargetPrice(new BigDecimal("100.00"));
        stock.setLastAlertPrice(new BigDecimal("100.00"));
        stock.setAlertTriggered(true);

        when(stockRepository.findById(1L))
                .thenReturn(Optional.of(stock));

        // Price falls below target → reset
        boolean firstResult = stockService.checkAndUpdateAlert(
                1L,
                new BigDecimal("99.00")
        );

        assertFalse(firstResult);
        assertFalse(stock.getAlertTriggered());
        assertNull(stock.getLastAlertPrice());

        // Price reaches target again → new alert
        boolean secondResult = stockService.checkAndUpdateAlert(
                1L,
                new BigDecimal("100.00")
        );

        assertTrue(secondResult);
        assertTrue(stock.getAlertTriggered());
        assertEquals(
                new BigDecimal("100.00"),
                stock.getLastAlertPrice()
        );
    }
    private StockResponse createStock(
            BigDecimal targetPrice,
            BigDecimal lastAlertPrice) {

        return new StockResponse(
                1L,
                "MU",
                "Micron Technology, Inc.",
                5,
                new BigDecimal("1015.60"),
                Currency.USD,
                targetPrice,
                "Testing alert",
                true,
                false,
                lastAlertPrice,
                null,
                null
        );
    }

}