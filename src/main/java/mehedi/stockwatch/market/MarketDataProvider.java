package mehedi.stockwatch.market;

import java.math.BigDecimal;

public interface MarketDataProvider {

    BigDecimal getCurrentPrice(String ticker);
}