package mehedi.stockwatch.market;

public record StockSearchResponse(
        String ticker,
        String companyName,
        String exchange,
        String quoteType
) {
}