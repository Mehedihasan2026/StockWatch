package mehedi.stockwatch.market;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketQuote(
        BigDecimal currentPrice,
        Instant lastUpdated,
        String marketStatus,
        String exchangeTimezone
) {
}
