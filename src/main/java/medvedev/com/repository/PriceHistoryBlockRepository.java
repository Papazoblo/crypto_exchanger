package medvedev.com.repository;

import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.BlockTimeType;
import medvedev.com.enums.PriceBlockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryBlockRepository extends JpaRepository<PriceHistoryBlockEntity, Long> {

    List<PriceHistoryBlockEntity> findAllByStatus(PriceBlockStatus status);

    Optional<PriceHistoryBlockEntity> findFirstByDateOpenLessThanAndStatusAndTimeTypeOrderByDateOpenDesc(LocalDateTime curDate,
                                                                                                         PriceBlockStatus status,
                                                                                                         BlockTimeType timeType);

    @Query(value = "select *\n" +
            "from cr_schema.price_history_block\n" +
            "where id in (select id\n" +
            "             from cr_schema.price_history_block\n" +
            "             where status = 'CLOSE'" +
            "               and time_type = :timeType \n" +
            "             order by id desc\n" +
            "             limit :limit)\n" +
            "order by id desc", nativeQuery = true)
    List<PriceHistoryBlockEntity> getLastLimitClosedBlocks(@RequestParam("timeType") String timeType,
                                                           @RequestParam("limit") int limit);


    @Query(value = "select *\n" +
            "from cr_schema.price_history_block\n" +
            "where id in (select id\n" +
            "             from cr_schema.price_history_block\n" +
            "             where status = 'CLOSE'" +
            "               and time_type = :timeType \n" +
            "               and id <= :id" +
            "             order by id desc\n" +
            "             limit :limit)\n" +
            "order by id desc", nativeQuery = true)
    List<PriceHistoryBlockEntity> getLastLimitClosedBlocks(@RequestParam("timeType") String timeType,
                                                           @RequestParam("limit") int limit,
                                                           @RequestParam("id") Long id);

    Optional<PriceHistoryBlockEntity> findFirstByStatusOrderByDateOpenDesc(PriceBlockStatus status);

    @Query("select pb from PriceHistoryBlockEntity pb " +
            "where pb.dateOpen < :date")
    List<PriceHistoryBlockEntity> findIdsByOpenDate(@Param("date") LocalDateTime date);
}
