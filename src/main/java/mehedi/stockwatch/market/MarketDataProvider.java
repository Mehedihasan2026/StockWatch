package mehedi.stockwatch.market;

import java.math.BigDecimal;

public interface MarketDataProvider {

    MarketQuote getQuote(String ticker);

    default BigDecimal getCurrentPrice(
            String ticker) {

        return getQuote(ticker)
                .currentPrice();
    }
}
