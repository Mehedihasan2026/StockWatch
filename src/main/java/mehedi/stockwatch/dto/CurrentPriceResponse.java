package mehedi.stockwatch.dto;

import mehedi.stockwatch.entity.Currency;

import java.math.BigDecimal;
import java.time.Instant;

public record CurrentPriceResponse(
        String ticker,
        BigDecimal currentPrice,
        Currency currency,
        Instant lastUpdated,
        String marketStatus,
        String exchangeTimezone
) {

    /*
     * Backwards-compatible constructor.
     * Existing internal callers can still create
     * a price-only response.
     */
    public CurrentPriceResponse(
            String ticker,
            BigDecimal currentPrice,
            Currency currency) {

        this(
                ticker,
                currentPrice,
                currency,
                null,
                "UNKNOWN",
                null
        );
    }
}
