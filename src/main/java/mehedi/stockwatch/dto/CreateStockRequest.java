package mehedi.stockwatch.dto;

import java.math.BigDecimal;

public record CreateStockRequest(
        String ticker,
        String companyName,
        Integer shares,
        BigDecimal buyPrice,
        String currency,
        BigDecimal targetPrice,
        String notes,
        Boolean alertEnabled
) {
}