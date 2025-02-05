package medvedev.com.repository;

import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.PriceBlockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "select *\n" +
            "from cr_schema.price_history_block\n" +
            "where id in (select id\n" +
            "             from cr_schema.price_history_block\n" +
            "             where status = 'CLOSE'\n" +
            "             order by id desc\n" +
            "             limit 3)\n" +
            "order by id desc", nativeQuery = true)
    List<PriceHistoryBlockEntity> getLast3ClosedBlocks();

    Optional<PriceHistoryBlockEntity> findFirstByStatusOrderByDateOpenDesc(PriceBlockStatus status);

    @Query("select pb from PriceHistoryBlockEntity pb " +
            "where pb.dateOpen < :date")
    List<PriceHistoryBlockEntity> findIdsByOpenDate(@Param("date") LocalDateTime date);
}
