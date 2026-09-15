package mehedi.stockwatch.controller;

import mehedi.stockwatch.dto.UpdateStockRequest;
import mehedi.stockwatch.dto.CreateStockRequest;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final MarketDataService marketDataService;
    public StockController(StockService stockService,
    MarketDataService marketDataService ) {
        this.stockService = stockService;
        this.marketDataService = marketDataService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse createStock(@Valid @RequestBody CreateStockRequest request) {
        return stockService.createStock(request);
    }

    @GetMapping
    public List<StockResponse> getAllStocks() {
        return stockService.getAllStocks();
    }

    @GetMapping("/{id}")
    public StockResponse getStockById(@PathVariable Long id) {
        return stockService.getStockById(id);
    }
    @PutMapping("/{id}")
    public StockResponse updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {

        return stockService.updateStock(id, request);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
    }
    @GetMapping("/{id}/price")
    public BigDecimal getCurrentPrice(@PathVariable Long id) {

        StockResponse stock = stockService.getStockById(id);

        return marketDataService.getCurrentPrice(stock.ticker());
    }
}