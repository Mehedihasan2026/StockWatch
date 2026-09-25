package mehedi.stockwatch.repository;

import mehedi.stockwatch.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Stock> findByAlertEnabledTrue();

    @Query("""
            select s.user.id
            from Stock s
            where s.id = :stockId
            """)
    Optional<Long> findOwnerUserIdByStockId(
            @Param("stockId")
            Long stockId
    );
}