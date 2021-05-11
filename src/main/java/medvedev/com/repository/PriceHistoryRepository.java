package medvedev.com.repository;

import medvedev.com.entity.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, Long> {

    List<PriceHistoryEntity> findAllByDateIsBefore(LocalDateTime dateTime);
}
