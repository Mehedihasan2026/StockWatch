package mehedi.stockwatch.controller;

import mehedi.stockwatch.dto.CreateStockRequest;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
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
}