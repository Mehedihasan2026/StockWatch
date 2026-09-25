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


    public MarketController(
            MarketScreenerService marketScreenerService
    ) {

        this.marketScreenerService =
                marketScreenerService;
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
}