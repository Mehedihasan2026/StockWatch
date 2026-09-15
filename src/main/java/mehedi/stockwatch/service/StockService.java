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
import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
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
}