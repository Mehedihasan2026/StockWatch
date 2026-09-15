package mehedi.stockwatch.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;

@Component
public class YahooFinanceProvider implements MarketDataProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YahooFinanceProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {

        this.restClient = restClientBuilder
                .baseUrl("https://query1.finance.yahoo.com")
                .build();

        this.objectMapper = objectMapper;
    }

    @Override
    public BigDecimal getCurrentPrice(String ticker) {

        try {
            String response = restClient.get()
                    .uri("/v8/finance/chart/{ticker}", ticker)
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);

            JsonNode priceNode = root
                    .path("chart")
                    .path("result")
                    .path(0)
                    .path("meta")
                    .path("regularMarketPrice");

            if (priceNode.isMissingNode() || priceNode.isNull()) {
                throw new MarketDataException(
                        "Current price not found for ticker: " + ticker
                );
            }

            return priceNode.decimalValue();

        } catch (HttpClientErrorException exception) {

            throw new MarketDataException(
                    "Failed to retrieve market data for ticker: " + ticker,
                    exception
            );
        } catch (JsonProcessingException exception) {

            throw new MarketDataException(
                    "Failed to parse market data for ticker: " + ticker,
                    exception
            );
        }
    }
}