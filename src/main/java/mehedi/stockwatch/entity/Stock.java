package mehedi.stockwatch.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "stocks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stocks_ticker", columnNames = "ticker")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(nullable = false)
    private Integer shares;

    @Column(name = "buy_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal buyPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "target_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal targetPrice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "alert_enabled", nullable = false)
    private Boolean alertEnabled = true;

    @Column(name = "alert_triggered", nullable = false)
    private Boolean alertTriggered = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
