package medvedev.com.repository;

import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.PriceBlockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryBlockRepository extends JpaRepository<PriceHistoryBlockEntity, Long> {

    List<PriceHistoryBlockEntity> findAllByStatus(PriceBlockStatus status);

    List<PriceHistoryBlockEntity> findAllByDateCloseGreaterThanAndStatusOrderByDateOpenDesc(LocalDateTime date,
                                                                                            PriceBlockStatus status);

    Optional<PriceHistoryBlockEntity> findFirstByDateOpenLessThanAndStatusOrderByDateOpenDesc(LocalDateTime curDate,
                                                                                              PriceBlockStatus status);

    Optional<PriceHistoryBlockEntity> findFirstByStatusOrderByDateOpenDesc(PriceBlockStatus status);
}
