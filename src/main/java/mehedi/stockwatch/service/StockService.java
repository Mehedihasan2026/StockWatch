package mehedi.stockwatch.service;

import mehedi.stockwatch.dto.CreateStockRequest;
import mehedi.stockwatch.dto.CurrentPriceResponse;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.dto.UpdateStockRequest;
import mehedi.stockwatch.entity.Stock;
import mehedi.stockwatch.entity.User;
import mehedi.stockwatch.exception.StockAlreadyExistsException;
import mehedi.stockwatch.exception.StockNotFoundException;
import mehedi.stockwatch.market.MarketDataService;
import mehedi.stockwatch.repository.StockRepository;
import mehedi.stockwatch.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final MarketDataService marketDataService;
    private final UserRepository userRepository;

    public StockService(
            StockRepository stockRepository,
            MarketDataService marketDataService,
            UserRepository userRepository) {

        this.stockRepository = stockRepository;
        this.marketDataService = marketDataService;
        this.userRepository = userRepository;
    }


    public StockResponse createStock(
            CreateStockRequest request,
            String userEmail) {

        User user =
                getUser(userEmail);

        String ticker =
                normalizeTicker(
                        request.ticker()
                );

        if (
                stockRepository
                        .existsByTickerIgnoreCaseAndUser_Id(
                                ticker,
                                user.getId()
                        )
        ) {

            throw new StockAlreadyExistsException(
                    "You already monitor "
                            + ticker
                            + "."
            );
        }

        Stock stock =
                new Stock();

        stock.setTicker(ticker);

        stock.setCompanyName(
                request.companyName()
        );

        stock.setShares(
                request.shares()
        );

        stock.setBuyPrice(
                request.buyPrice()
        );

        stock.setCurrency(
                request.currency()
        );

        stock.setTargetPrice(
                request.targetPrice()
        );

        stock.setNotes(
                request.notes()
        );

        stock.setAlertEnabled(
                request.alertEnabled() != null
                        ? request.alertEnabled()
                        : true
        );

        stock.setAlertTriggered(false);

        stock.setLastAlertPrice(null);

        stock.setUser(user);

        LocalDateTime now =
                LocalDateTime.now();

        stock.setCreatedAt(now);
        stock.setUpdatedAt(now);

        return toResponse(
                stockRepository.save(stock)
        );
    }


    public List<StockResponse> getAllStocks(
            String userEmail) {

        User user =
                getUser(userEmail);

        return stockRepository
                .findAllByUser_IdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public StockResponse getStockById(
            Long id,
            String userEmail) {

        User user =
                getUser(userEmail);

        Stock stock =
                findOwnedStock(
                        id,
                        user.getId()
                );

        return toResponse(stock);
    }


    public StockResponse updateStock(
            Long id,
            UpdateStockRequest request,
            String userEmail) {

        User user =
                getUser(userEmail);

        Stock stock =
                findOwnedStock(
                        id,
                        user.getId()
                );

        String ticker =
                normalizeTicker(
                        request.ticker()
                );

        boolean duplicateTicker =
                stockRepository
                        .existsByTickerIgnoreCaseAndUser_IdAndIdNot(
                                ticker,
                                user.getId(),
                                id
                        );

        if (duplicateTicker) {

            throw new StockAlreadyExistsException(
                    "You already monitor "
                            + ticker
                            + "."
            );
        }

        boolean targetPriceChanged =
                stock.getTargetPrice()
                        .compareTo(
                                request.targetPrice()
                        ) != 0;

        stock.setTicker(ticker);

        stock.setCompanyName(
                request.companyName()
        );

        stock.setShares(
                request.shares()
        );

        stock.setBuyPrice(
                request.buyPrice()
        );

        stock.setCurrency(
                request.currency()
        );

        stock.setTargetPrice(
                request.targetPrice()
        );

        stock.setNotes(
                request.notes()
        );

        stock.setAlertEnabled(
                request.alertEnabled() != null
                        ? request.alertEnabled()
                        : stock.getAlertEnabled()
        );

        /*
         * A changed target starts a new alert cycle.
         */
        if (targetPriceChanged) {

            stock.setAlertTriggered(false);
            stock.setLastAlertPrice(null);
        }

        stock.setUpdatedAt(
                LocalDateTime.now()
        );

        return toResponse(
                stockRepository.save(stock)
        );
    }


    public void deleteStock(
            Long id,
            String userEmail) {

        User user =
                getUser(userEmail);

        Stock stock =
                findOwnedStock(
                        id,
                        user.getId()
                );

        stockRepository.delete(stock);
    }


    public CurrentPriceResponse getCurrentPrice(
            Long id,
            String userEmail) {

        User user =
                getUser(userEmail);

        Stock stock =
                findOwnedStock(
                        id,
                        user.getId()
                );

        BigDecimal currentPrice =
                marketDataService
                        .getCurrentPrice(
                                stock.getTicker()
                        );

        return new CurrentPriceResponse(
                stock.getTicker(),
                currentPrice,
                stock.getCurrency()
        );
    }


    /*
     * Background scheduler method.
     *
     * This intentionally returns alert-enabled
     * stocks across all users.
     */
    public List<StockResponse>
    getStocksForMonitoring() {

        return stockRepository
                .findByAlertEnabledTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public boolean shouldTriggerAlert(
            StockResponse stock,
            BigDecimal currentPrice) {

        if (
                currentPrice.compareTo(
                        stock.targetPrice()
                ) < 0
        ) {
            return false;
        }

        if (
                stock.lastAlertPrice()
                        == null
        ) {
            return true;
        }

        BigDecimal threshold =
                stock.targetPrice()
                        .multiply(
                                BigDecimal
                                        .valueOf(0.01)
                        );

        BigDecimal priceMovement =
                currentPrice
                        .subtract(
                                stock.lastAlertPrice()
                        )
                        .abs();

        return priceMovement
                .compareTo(threshold)
                >= 0;
    }


    @Transactional
    public boolean checkAndUpdateAlert(
            Long stockId,
            BigDecimal currentPrice) {

        Stock stock =
                stockRepository
                        .findById(stockId)
                        .orElseThrow(() ->
                                new StockNotFoundException(
                                        "Stock not found with id: "
                                                + stockId
                                )
                        );

        if (
                currentPrice.compareTo(
                        stock.getTargetPrice()
                ) < 0
        ) {

            if (
                    stock.getLastAlertPrice()
                            != null
                            ||
                            Boolean.TRUE.equals(
                                    stock.getAlertTriggered()
                            )
            ) {

                stock.setLastAlertPrice(
                        null
                );

                stock.setAlertTriggered(
                        false
                );

                stockRepository.save(
                        stock
                );
            }

            return false;
        }

        StockResponse stockResponse =
                toResponse(stock);

        if (
                !shouldTriggerAlert(
                        stockResponse,
                        currentPrice
                )
        ) {
            return false;
        }

        stock.setLastAlertPrice(
                currentPrice
        );

        stock.setAlertTriggered(
                true
        );

        stockRepository.save(stock);

        return true;
    }


    private Stock findOwnedStock(
            Long stockId,
            Long userId) {

        return stockRepository
                .findByIdAndUser_Id(
                        stockId,
                        userId
                )
                .orElseThrow(() ->
                        new StockNotFoundException(
                                "Stock not found: "
                                        + stockId
                        )
                );
    }


    private User getUser(
            String email) {

        return userRepository
                .findByEmailIgnoreCase(
                        email
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found."
                        )
                );
    }


    private String normalizeTicker(
            String ticker) {

        return ticker
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }


    private StockResponse toResponse(
            Stock stock) {

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
}