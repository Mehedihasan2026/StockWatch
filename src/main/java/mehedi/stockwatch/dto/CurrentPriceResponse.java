package mehedi.stockwatch.dto;

import mehedi.stockwatch.entity.Currency;

import java.math.BigDecimal;

public record CurrentPriceResponse(
        String ticker,
        BigDecimal currentPrice,
        Currency currency
) {
}