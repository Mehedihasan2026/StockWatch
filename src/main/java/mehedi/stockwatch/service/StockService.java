package mehedi.stockwatch.service;
import mehedi.stockwatch.exception.StockAlreadyExistsException;
import mehedi.stockwatch.exception.StockNotFoundException;
import mehedi.stockwatch.repository.StockRepository;
import org.springframework.stereotype.Service;
import mehedi.stockwatch.dto.CreateStockRequest;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.entity.Stock;
import mehedi.stockwatch.dto.UpdateStockRequest;
import java.time.LocalDateTime;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.dto.CurrentPriceResponse;
import java.math.BigDecimal;
import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final MarketDataService marketDataService;
    public StockService(
            StockRepository stockRepository,
            MarketDataService marketDataService) {

        this.stockRepository = stockRepository;
        this.marketDataService = marketDataService;
    }
    public StockResponse createStock(CreateStockRequest request) {

        Stock stock = new Stock();

        stock.setTicker(request.ticker());
        stock.setCompanyName(request.companyName());
        stock.setShares(request.shares());
        stock.setBuyPrice(request.buyPrice());
        stock.setCurrency(request.currency());
        stock.setTargetPrice(request.targetPrice());
        stock.setNotes(request.notes());

        stock.setAlertEnabled(
                request.alertEnabled() != null
                        ? request.alertEnabled()
                        : true
        );

        stock.setAlertTriggered(false);

        LocalDateTime now = LocalDateTime.now();
        stock.setCreatedAt(now);
        stock.setUpdatedAt(now);

        if (stockRepository.existsByTicker(request.ticker())) {
            throw new StockAlreadyExistsException(
                    "A stock with ticker " + request.ticker() + " already exists."
            );
        }
        Stock savedStock = stockRepository.save(stock);

        return toResponse(savedStock);
    }
    private StockResponse toResponse(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getTicker(),
                stock.getCompanyName(),
                stock.getShares(),
                stock.getBuyPrice(),
                stock.getCurrency(),
                stock.getTargetPrice(),
                stock.getNotes(),
                stock.getAlertEnabled(),
                stock.getAlertTriggered(),
                stock.getLastAlertPrice(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }
    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    public StockResponse getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() ->
                        new StockNotFoundException("Stock not found: " + id)
                );

        return toResponse(stock);
    }
    public StockResponse updateStock(Long id, UpdateStockRequest request) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() ->
                        new StockNotFoundException("Stock not found: " + id)
                );

        stock.setTicker(request.ticker());
        stock.setCompanyName(request.companyName());
        stock.setShares(request.shares());
        stock.setBuyPrice(request.buyPrice());
        stock.setCurrency(request.currency());
        stock.setTargetPrice(request.targetPrice());
        stock.setNotes(request.notes());

        stock.setAlertEnabled(
                request.alertEnabled() != null
                        ? request.alertEnabled()
                        : stock.getAlertEnabled()
        );

        stock.setUpdatedAt(LocalDateTime.now());

        Stock updatedStock = stockRepository.save(stock);

        return toResponse(updatedStock);
    }
    public void deleteStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() ->
                        new StockNotFoundException("Stock not found: " + id)
                );

        stockRepository.delete(stock);
    }
    public CurrentPriceResponse getCurrentPrice(Long id) {

        StockResponse stock = getStockById(id);

        BigDecimal currentPrice =
                marketDataService.getCurrentPrice(stock.ticker());

        return new CurrentPriceResponse(
                stock.ticker(),
                currentPrice,
                stock.currency()
        );
    }
    public List<StockResponse> getStocksForMonitoring() {

        return stockRepository.findByAlertEnabledTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    public boolean shouldTriggerAlert(
            StockResponse stock,
            BigDecimal currentPrice) {

        // Price must be at or above the target
        if (currentPrice.compareTo(stock.targetPrice()) < 0) {
            return false;
        }

        // First time reaching the target
        if (stock.lastAlertPrice() == null) {
            return true;
        }

        // 1% of the target price
        BigDecimal threshold =
                stock.targetPrice()
                        .multiply(BigDecimal.valueOf(0.01));

        // Calculate absolute movement since the last alert
        BigDecimal priceMovement =
                currentPrice.subtract(stock.lastAlertPrice()).abs();

        return priceMovement.compareTo(threshold) >= 0;
    }
}