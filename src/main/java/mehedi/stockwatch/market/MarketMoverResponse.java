package mehedi.stockwatch.market;

import java.math.BigDecimal;

public record MarketMoverResponse(
        String ticker,
        String companyName,
        BigDecimal price,
        BigDecimal change,
        BigDecimal changePercent,
        Long volume,
        String currency,
        String exchange
) {
}