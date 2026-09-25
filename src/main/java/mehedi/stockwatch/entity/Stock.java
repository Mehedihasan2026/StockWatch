package mehedi.stockwatch.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private Integer shares;

    @Column(name = "buy_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal buyPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(name = "target_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal targetPrice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "alert_enabled", nullable = false)
    private Boolean alertEnabled;

    @Column(name = "alert_triggered", nullable = false)
    private Boolean alertTriggered;

    @Column(name = "last_alert_price", precision = 12, scale = 4)
    private BigDecimal lastAlertPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getShares() {
        return shares;
    }

    public void setShares(Integer shares) {
        this.shares = shares;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertEnabled(Boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public Boolean getAlertTriggered() {
        return alertTriggered;
    }

    public void setAlertTriggered(Boolean alertTriggered) {
        this.alertTriggered = alertTriggered;
    }

    public BigDecimal getLastAlertPrice() {
        return lastAlertPrice;
    }

    public void setLastAlertPrice(BigDecimal lastAlertPrice) {
        this.lastAlertPrice = lastAlertPrice;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}