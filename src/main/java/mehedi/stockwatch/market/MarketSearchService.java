package mehedi.stockwatch.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketSearchService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;


    public MarketSearchService(
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


    public List<StockSearchResponse> search(
            String query,
            int requestedLimit
    ) {

        if (
                query == null
                        ||
                        query.trim().isEmpty()
        ) {

            return List.of();
        }


        int limit =
                Math.max(
                        1,
                        Math.min(
                                requestedLimit,
                                10
                        )
                );


        try {

            String response =
                    restClient
                            .get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(
                                                    "/v1/finance/search"
                                            )
                                            .queryParam(
                                                    "q",
                                                    query.trim()
                                            )
                                            .queryParam(
                                                    "quotesCount",
                                                    limit
                                            )
                                            .queryParam(
                                                    "newsCount",
                                                    0
                                            )
                                            .queryParam(
                                                    "listsCount",
                                                    0
                                            )
                                            .queryParam(
                                                    "enableFuzzyQuery",
                                                    false
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

                return List.of();
            }


            JsonNode root =
                    objectMapper.readTree(
                            response
                    );


            JsonNode quotes =
                    root.path(
                            "quotes"
                    );


            if (!quotes.isArray()) {

                return List.of();
            }


            List<StockSearchResponse> results =
                    new ArrayList<>();


            for (JsonNode quote : quotes) {

                String quoteType =
                        text(
                                quote,
                                "quoteType"
                        );


                /*
                 * For StockWatch we only want
                 * actual stocks and ETFs.
                 */
                if (
                        !"EQUITY".equalsIgnoreCase(
                                quoteType
                        )
                                &&
                                !"ETF".equalsIgnoreCase(
                                        quoteType
                                )
                ) {

                    continue;
                }


                String ticker =
                        text(
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
                        firstNonBlank(
                                text(
                                        quote,
                                        "longname"
                                ),
                                text(
                                        quote,
                                        "shortname"
                                ),
                                ticker
                        );


                String exchange =
                        firstNonBlank(
                                text(
                                        quote,
                                        "exchDisp"
                                ),
                                text(
                                        quote,
                                        "exchange"
                                ),
                                ""
                        );


                results.add(
                        new StockSearchResponse(
                                ticker,
                                companyName,
                                exchange,
                                quoteType
                        )
                );


                if (
                        results.size()
                                >=
                                limit
                ) {

                    break;
                }
            }


            return results;

        } catch (Exception exception) {

            throw new MarketDataException(
                    "Could not search Yahoo Finance.",
                    exception
            );
        }
    }


    private String text(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(
                        field
                );


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


    private String firstNonBlank(
            String... values
    ) {

        for (String value : values) {

            if (
                    value != null
                            &&
                            !value.isBlank()
            ) {

                return value;
            }
        }


        return "";
    }
}