package mehedi.stockwatch.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YahooFinanceProviderTest {

    @Test
    void shouldCallYahooFinance() {

        RestClient.Builder builder = RestClient.builder();
        ObjectMapper objectMapper = new ObjectMapper();

        YahooFinanceProvider provider =
                new YahooFinanceProvider(builder, objectMapper);

        BigDecimal price = provider.getCurrentPrice("MU");

        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }
    @Test
    void shouldRejectInvalidTicker() {

        RestClient.Builder builder = RestClient.builder();
        ObjectMapper objectMapper = new ObjectMapper();

        YahooFinanceProvider provider =
                new YahooFinanceProvider(builder, objectMapper);

        assertThrows(
                MarketDataException.class,
                () -> provider.getCurrentPrice("THIS_IS_NOT_A_REAL_TICKER")
        );
    }
}