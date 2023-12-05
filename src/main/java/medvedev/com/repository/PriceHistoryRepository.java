package medvedev.com.repository;

import medvedev.com.entity.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, LocalDateTime> {

    @Query(value = "select cast(coalesce((max(cast(ph.price as DECIMAL)) - min(cast(ph.price as DECIMAL))) > 100, false) as varchar)\n" +
            "from price_history ph\n" +
            "where ph.date > (current_timestamp - interval '1 hour')", nativeQuery = true)
    boolean isPriceDifferenceLong();

    Optional<PriceHistoryEntity> findFirstByDateIsNotNullOrderByDateDesc();

    List<PriceHistoryEntity> findAllByDateGreaterThanOrderByDateDesc(LocalDateTime time);
}
