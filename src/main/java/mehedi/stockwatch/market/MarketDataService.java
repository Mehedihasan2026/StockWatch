package mehedi.stockwatch.market;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MarketDataService {

    private final MarketDataProvider
            marketDataProvider;

    public MarketDataService(
            MarketDataProvider marketDataProvider) {

        this.marketDataProvider =
                marketDataProvider;
    }

    public MarketQuote getQuote(
            String ticker) {

        return marketDataProvider
                .getQuote(ticker);
    }

    public BigDecimal getCurrentPrice(
            String ticker) {

        return marketDataProvider
                .getCurrentPrice(ticker);
    }
}
