package mehedi.stockwatch.repository;

import mehedi.stockwatch.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {

    boolean existsByTicker(String ticker);
    List<Stock> findByAlertEnabledTrue();
}