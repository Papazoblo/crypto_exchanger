package medvedev.com.repository;

import medvedev.com.entity.PricePredictionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PricePredictionHistoryRepository extends JpaRepository<PricePredictionHistoryEntity, LocalDateTime> {

    Optional<PricePredictionHistoryEntity> findTopByDateIsBeforeOrderByDateDesc(LocalDateTime currentDateTime);
}
