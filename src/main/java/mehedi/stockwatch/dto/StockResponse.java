package mehedi.stockwatch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockResponse(
        Long id,
        String ticker,
        String companyName,
        Integer shares,
        BigDecimal buyPrice,
        String currency,
        BigDecimal targetPrice,
        String notes,
        Boolean alertEnabled,
        Boolean alertTriggered,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}