package medvedev.com.repository;

import medvedev.com.entity.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, LocalDateTime> {

    Optional<PriceHistoryEntity> findFirstByDateIsNotNullOrderByDateDesc();
}
