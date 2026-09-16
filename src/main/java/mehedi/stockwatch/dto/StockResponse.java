package mehedi.stockwatch.dto;

import mehedi.stockwatch.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockResponse(
        Long id,
        String ticker,
        String companyName,
        Integer shares,
        BigDecimal buyPrice,
        Currency currency,
        BigDecimal targetPrice,
        String notes,
        Boolean alertEnabled,
        Boolean alertTriggered,
        BigDecimal lastAlertPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}