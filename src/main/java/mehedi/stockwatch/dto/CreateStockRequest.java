package mehedi.stockwatch.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateStockRequest(

        @NotBlank(message = "Ticker is required")
        @Size(max = 20, message = "Ticker must not exceed 20 characters")
        String ticker,

        @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must not exceed 255 characters")
        String companyName,

        @NotNull(message = "Shares are required")
        @Positive(message = "Shares must be greater than 0")
        Integer shares,

        @NotNull(message = "Buy price is required")
        @DecimalMin(value = "0.0001", message = "Buy price must be greater than 0")
        BigDecimal buyPrice,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
        String currency,

        @NotNull(message = "Target price is required")
        @DecimalMin(value = "0.0001", message = "Target price must be greater than 0")
        BigDecimal targetPrice,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes,

        Boolean alertEnabled
) {
}