package mehedi.stockwatch.repository;

import mehedi.stockwatch.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository
        extends JpaRepository<Stock, Long> {

    boolean existsByTickerIgnoreCaseAndUser_Id(
            String ticker,
            Long userId
    );

    boolean existsByTickerIgnoreCaseAndUser_IdAndIdNot(
            String ticker,
            Long userId,
            Long id
    );

    List<Stock> findAllByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Stock> findByIdAndUser_Id(
            Long id,
            Long userId
    );

    /*
     * Used by the background scheduler.
     * The scheduler monitors enabled stocks for all users.
     */
    List<Stock> findByAlertEnabledTrue();
}