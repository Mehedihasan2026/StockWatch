package mehedi.stockwatch.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarketScreenerService {

    private static final Duration CACHE_DURATION =
            Duration.ofSeconds(60);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private List<MarketMoverResponse> cachedGainers =
            List.of();

    private Instant cacheTime =
            Instant.EPOCH;


    public MarketScreenerService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {

        this.restClient =
                restClientBuilder
                        .baseUrl(
                                "https://query1.finance.yahoo.com"
                        )
                        .build();

        this.objectMapper =
                objectMapper;
    }


    public synchronized List<MarketMoverResponse>
    getTopGainers(
            int requestedLimit
    ) {

        int limit =
                Math.max(
                        1,
                        Math.min(
                                requestedLimit,
                                20
                        )
                );


        if (
                !cachedGainers.isEmpty()
                        &&
                        Instant.now()
                                .isBefore(
                                        cacheTime.plus(
                                                CACHE_DURATION
                                        )
                                )
        ) {

            return cachedGainers
                    .stream()
                    .limit(limit)
                    .toList();
        }


        List<MarketMoverResponse> fresh =
                fetchTopGainers();


        cachedGainers =
                List.copyOf(fresh);

        cacheTime =
                Instant.now();


        return cachedGainers
                .stream()
                .limit(limit)
                .toList();
    }


    private List<MarketMoverResponse>
    fetchTopGainers() {

        try {

            String response =
                    restClient
                            .get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(
                                                    "/v1/finance/screener/predefined/saved"
                                            )
                                            .queryParam(
                                                    "formatted",
                                                    "false"
                                            )
                                            .queryParam(
                                                    "lang",
                                                    "en-US"
                                            )
                                            .queryParam(
                                                    "region",
                                                    "US"
                                            )
                                            .queryParam(
                                                    "scrIds",
                                                    "day_gainers"
                                            )
                                            .queryParam(
                                                    "count",
                                                    20
                                            )
                                            .queryParam(
                                                    "corsDomain",
                                                    "finance.yahoo.com"
                                            )
                                            .build()
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
                        "Yahoo Finance returned an empty response."
                );
            }


            JsonNode root =
                    objectMapper
                            .readTree(
                                    response
                            );


            JsonNode quotes =
                    root
                            .path("finance")
                            .path("result")
                            .path(0)
                            .path("quotes");


            if (!quotes.isArray()) {

                throw new MarketDataException(
                        "Yahoo Finance returned an unexpected screener response."
                );
            }


            List<MarketMoverResponse> results =
                    new ArrayList<>();


            for (JsonNode quote : quotes) {

                String ticker =
                        textValue(
                                quote,
                                "symbol"
                        );


                if (
                        ticker == null
                                ||
                                ticker.isBlank()
                ) {
                    continue;
                }


                String companyName =
                        firstAvailableText(
                                quote,
                                "longName",
                                "shortName",
                                "displayName"
                        );


                if (
                        companyName == null
                                ||
                                companyName.isBlank()
                ) {

                    companyName =
                            ticker;
                }


                results.add(
                        new MarketMoverResponse(

                                ticker,

                                companyName,

                                decimalValue(
                                        quote,
                                        "regularMarketPrice"
                                ),

                                decimalValue(
                                        quote,
                                        "regularMarketChange"
                                ),

                                decimalValue(
                                        quote,
                                        "regularMarketChangePercent"
                                ),

                                longValue(
                                        quote,
                                        "regularMarketVolume"
                                ),

                                textValue(
                                        quote,
                                        "currency"
                                ),

                                firstAvailableText(
                                        quote,
                                        "fullExchangeName",
                                        "exchange"
                                )
                        )
                );
            }


            return results;

        } catch (
                RestClientException
                |
                JsonProcessingException exception
        ) {

            /*
             * If Yahoo temporarily fails but we
             * already have previous data, return it.
             */
            if (!cachedGainers.isEmpty()) {

                return cachedGainers;
            }


            throw new MarketDataException(
                    "Failed to retrieve market gainers from Yahoo Finance.",
                    exception
            );
        }
    }


    private String textValue(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.path(field);


        if (
                value.isMissingNode()
                        ||
                        value.isNull()
        ) {

            return null;
        }


        return value.asText();
    }


    private String firstAvailableText(
            JsonNode node,
            String... fields
    ) {

        for (String field : fields) {

            String value =
                    textValue(
                            node,
                            field
                    );


            if (
                    value != null
                            &&
                            !value.isBlank()
            ) {

                return value;
            }
        }


        return null;
    }


    private BigDecimal decimalValue(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.path(field);


        if (
                value.isMissingNode()
                        ||
                        value.isNull()
                        ||
                        !value.isNumber()
        ) {

            return null;
        }


        return value.decimalValue();
    }


    private Long longValue(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.path(field);


        if (
                value.isMissingNode()
                        ||
                        value.isNull()
                        ||
                        !value.isNumber()
        ) {

            return null;
        }


        return value.longValue();
    }
}