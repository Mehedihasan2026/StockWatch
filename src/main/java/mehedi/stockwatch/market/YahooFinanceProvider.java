package mehedi.stockwatch.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class YahooFinanceProvider
        implements MarketDataProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YahooFinanceProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {

        this.restClient =
                restClientBuilder
                        .baseUrl(
                                "https://query1.finance.yahoo.com"
                        )
                        .build();

        this.objectMapper =
                objectMapper;
    }

    @Override
    public MarketQuote getQuote(
            String ticker) {

        try {

            String response =
                    restClient
                            .get()
                            .uri(
                                    "/v8/finance/chart/{ticker}",
                                    ticker
                            )
                            .header(
                                    "User-Agent",
                                    "Mozilla/5.0"
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );


            if (response == null) {

                throw new MarketDataException(
                        "Empty market data response for ticker: "
                                + ticker
                );
            }


            JsonNode root =
                    objectMapper
                            .readTree(
                                    response
                            );


            JsonNode meta =
                    root
                            .path("chart")
                            .path("result")
                            .path(0)
                            .path("meta");


            JsonNode priceNode =
                    meta.path(
                            "regularMarketPrice"
                    );


            if (
                    priceNode.isMissingNode()
                            ||
                            priceNode.isNull()
            ) {

                throw new MarketDataException(
                        "Current price not found for ticker: "
                                + ticker
                );
            }


            BigDecimal currentPrice =
                    priceNode.decimalValue();


            Instant lastUpdated =
                    readLastUpdated(
                            meta
                    );


            String marketStatus =
                    determineMarketStatus(
                            meta
                    );


            String exchangeTimezone =
                    readText(
                            meta,
                            "exchangeTimezoneName"
                    );


            return new MarketQuote(
                    currentPrice,
                    lastUpdated,
                    marketStatus,
                    exchangeTimezone
            );

        } catch (HttpClientErrorException exception) {

            throw new MarketDataException(
                    "Failed to retrieve market data for ticker: "
                            + ticker,
                    exception
            );

        } catch (JsonProcessingException exception) {

            throw new MarketDataException(
                    "Failed to parse market data for ticker: "
                            + ticker,
                    exception
            );
        }
    }


    private Instant readLastUpdated(
            JsonNode meta) {

        JsonNode marketTime =
                meta.path(
                        "regularMarketTime"
                );


        if (
                !marketTime.canConvertToLong()
                        ||
                        marketTime.asLong() <= 0
        ) {

            return null;
        }


        return Instant.ofEpochSecond(
                marketTime.asLong()
        );
    }


    private String determineMarketStatus(
            JsonNode meta) {

        JsonNode regularPeriod =
                meta
                        .path(
                                "currentTradingPeriod"
                        )
                        .path(
                                "regular"
                        );


        JsonNode startNode =
                regularPeriod.path(
                        "start"
                );

        JsonNode endNode =
                regularPeriod.path(
                        "end"
                );


        if (
                !startNode.canConvertToLong()
                        ||
                        !endNode.canConvertToLong()
        ) {

            return "UNKNOWN";
        }


        long start =
                startNode.asLong();

        long end =
                endNode.asLong();

        long now =
                Instant.now()
                        .getEpochSecond();


        return (
                now >= start
                        &&
                        now < end
        )
                ? "OPEN"
                : "CLOSED";
    }


    private String readText(
            JsonNode node,
            String field) {

        JsonNode value =
                node.get(field);


        if (
                value == null
                        ||
                        value.isNull()
        ) {

            return null;
        }


        String text =
                value.asText();


        return text.isBlank()
                ? null
                : text;
    }
}
