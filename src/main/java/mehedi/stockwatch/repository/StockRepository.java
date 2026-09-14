package mehedi.stockwatch.repository;

import mehedi.stockwatch.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

    boolean existsByTicker(String ticker);
}