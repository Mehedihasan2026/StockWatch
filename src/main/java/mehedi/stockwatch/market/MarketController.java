package mehedi.stockwatch.market;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketScreenerService marketScreenerService;

    private final MarketSearchService marketSearchService;


    public MarketController(
            MarketScreenerService marketScreenerService,
            MarketSearchService marketSearchService
    ) {

        this.marketScreenerService =
                marketScreenerService;

        this.marketSearchService =
                marketSearchService;
    }


    @GetMapping("/top-gainers")
    public List<MarketMoverResponse>
    getTopGainers(

            @RequestParam(
                    defaultValue = "20"
            )
            int limit
    ) {

        return marketScreenerService
                .getTopGainers(
                        limit
                );
    }


    @GetMapping("/search")
    public List<StockSearchResponse>
    searchStocks(

            @RequestParam
            String q,

            @RequestParam(
                    defaultValue = "6"
            )
            int limit
    ) {

        return marketSearchService
                .search(
                        q,
                        limit
                );
    }
}