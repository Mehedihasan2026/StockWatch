package mehedi.stockwatch.scheduler;

import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.service.StockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import mehedi.stockwatch.market.MarketDataService;
import java.math.BigDecimal;

import java.util.List;

@Component
public class StockPriceScheduler {

    private final StockService stockService;
    private final MarketDataService marketDataService;

    public StockPriceScheduler(
            StockService stockService,
            MarketDataService marketDataService) {

        this.stockService = stockService;
        this.marketDataService = marketDataService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkStockPrices() {

        List<StockResponse> stocks =
                stockService.getStocksForMonitoring();

        for (StockResponse stock : stocks) {

            BigDecimal currentPrice =
                    marketDataService.getCurrentPrice(stock.ticker());

            System.out.println(
                    stock.ticker() + " current price: " + currentPrice
            );
            if (currentPrice.compareTo(stock.targetPrice()) >= 0) {

                System.out.println(
                        stock.ticker() + " has reached its target price!"
                );
            }
        }
    }
}