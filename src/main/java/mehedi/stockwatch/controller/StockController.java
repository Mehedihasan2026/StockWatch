package mehedi.stockwatch.controller;

import jakarta.validation.Valid;
import mehedi.stockwatch.dto.CreateStockRequest;
import mehedi.stockwatch.dto.CurrentPriceResponse;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.dto.UpdateStockRequest;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.market.MarketQuote;
import mehedi.stockwatch.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    private final MarketDataService
            marketDataService;


    public StockController(
            StockService stockService,
            MarketDataService marketDataService) {

        this.stockService =
                stockService;

        this.marketDataService =
                marketDataService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse createStock(
            @Valid
            @RequestBody
            CreateStockRequest request,
            Authentication authentication) {

        return stockService
                .createStock(
                        request,
                        authentication.getName()
                );
    }


    @GetMapping
    public List<StockResponse> getAllStocks(
            Authentication authentication) {

        return stockService
                .getAllStocks(
                        authentication.getName()
                );
    }


    @GetMapping("/{id}")
    public StockResponse getStockById(
            @PathVariable Long id,
            Authentication authentication) {

        return stockService
                .getStockById(
                        id,
                        authentication.getName()
                );
    }


    @PutMapping("/{id}")
    public StockResponse updateStock(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateStockRequest request,
            Authentication authentication) {

        return stockService
                .updateStock(
                        id,
                        request,
                        authentication.getName()
                );
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStock(
            @PathVariable Long id,
            Authentication authentication) {

        stockService.deleteStock(
                id,
                authentication.getName()
        );
    }


    @GetMapping("/{id}/price")
    public CurrentPriceResponse getCurrentPrice(
            @PathVariable Long id,
            Authentication authentication) {

        StockResponse stock =
                stockService
                        .getStockById(
                                id,
                                authentication
                                        .getName()
                        );


        MarketQuote quote =
                marketDataService
                        .getQuote(
                                stock.ticker()
                        );


        return new CurrentPriceResponse(
                stock.ticker(),
                quote.currentPrice(),
                stock.currency(),
                quote.lastUpdated(),
                quote.marketStatus(),
                quote.exchangeTimezone()
        );
    }
}
