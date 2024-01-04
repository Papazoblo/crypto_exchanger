package medvedev.com.repository;

import medvedev.com.entity.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, LocalDateTime> {

    @Query(value = "select cast((coalesce(((select cast(ph_in.price as DECIMAL)\n" +
            "                       from price_history ph_in\n" +
            "                       where ph_in.date = min(ph.date)) -\n" +
            "                      (select cast(ph_in.price as DECIMAL)\n" +
            "                       from price_history ph_in\n" +
            "                       where ph_in.date = max(ph.date))) > :bound\n" +
            "                         or (select cast(ph.price as DECIMAL) > (select cast(pho.price as DECIMAL)\n" +
            "                                                                 from price_history pho\n" +
            "                                                                 order by pho.date desc\n" +
            "                                                                 limit 1)\n" +
            "                             from price_history_block phb\n" +
            "                                      join price_history ph on phb.id = ph.history_block_id\n" +
            "                             where phb.status = 'CLOSE'\n" +
            "                             order by ph.date desc\n" +
            "                             limit 1), false)\n" +
            "                    or (select phb.avg_change_type = 'DECREASED'\n" +
            "                        from price_history_block phb\n" +
            "                        where phb.status = 'OPEN')) as varchar)\n" +
            "--     min(ph.date) as old,\n" +
            "--     (select ph_in.price\n" +
            "--      from price_history ph_in\n" +
            "--      where ph_in.date = min(ph.date))  as old1,\n" +
            "--     max(ph.date)  as new,\n" +
            "--     (select ph_in.price\n" +
            "--      from price_history ph_in\n" +
            "--      where ph_in.date = max(ph.date)) as new1\n" +
            "from price_history ph\n" +
            "where ph.date > (current_timestamp - interval '5 minutes')", nativeQuery = true)
    boolean isPriceDifferenceLong(@Param("bound") int bound);

    Optional<PriceHistoryEntity> findFirstByDateIsNotNullOrderByDateDesc();

    List<PriceHistoryEntity> findAllByDateGreaterThanOrderByDateDesc(LocalDateTime time);
}
