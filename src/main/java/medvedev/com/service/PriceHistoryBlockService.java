package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.event.CandleCloseEvent;
import medvedev.com.dto.event.CreateBuyOrderEvent;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.BlockTimeType;
import medvedev.com.enums.PriceBlockStatus;
import medvedev.com.repository.PriceHistoryBlockRepository;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class PriceHistoryBlockService {

    private static final int HISTORY_LIST_SIZE = 10;

    private final BinanceClientService binanceClientService;
    private final PriceHistoryBlockRepository repository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher eventPublisher;


    //фиксирует курс блоками (мин, макс, средний)
    @Scheduled(cron = "${exchange.cron.fixed-price-block}")
    public void createPriceHistoryBlockScheduler() {
        LocalDateTime curDate = LocalDateTime.now();
        Arrays.stream(BlockTimeType.values())
                .filter(blockTimeType -> !blockTimeType.isExclude())
                .forEach(blockTimeType -> {
                    getLastBlock(curDate, blockTimeType).ifPresentOrElse(block -> {
                        //закрываем и открываем новый
                        if (block.getDateOpen().isBefore(curDate.minusMinutes(blockTimeType.getMinutes()).plusSeconds(30))) {
                            close(block, curDate);
                            create(blockTimeType);
                        } else { //обновляем
                            refresh(block);
                        }
                    }, () -> {
                        create(blockTimeType);//открываем
                    });
                    repository.deleteAll(repository.findIdsByOpenDate(LocalDateTime.now().minusDays(7)));
                });
    }

    public Optional<PriceHistoryBlockEntity> getLastBlock(LocalDateTime curDateTime, BlockTimeType blockTimeType) {
        return repository.findFirstByDateOpenLessThanAndStatusAndTimeTypeOrderByDateOpenDesc(curDateTime, PriceBlockStatus.OPEN, blockTimeType);
    }

    public List<PriceHistoryBlockEntity> getLastLimitBlock(BlockTimeType timeType, int limit) {
        return repository.getLastLimitClosedBlocks(timeType.name(), limit);
    }

    public List<PriceHistoryBlockEntity> getLastLimitBlock(Long id, BlockTimeType timeType, int limit) {
        return repository.getLastLimitClosedBlocks(timeType.name(), limit, id);
    }

    public void create(BlockTimeType blockTimeType) {
        PriceHistoryBlockEntity entity = new PriceHistoryBlockEntity();
        entity.setTimeType(blockTimeType);
        refresh(entity);
        repository.save(entity);
    }

    public void close(PriceHistoryBlockEntity entity, LocalDateTime curDate) {

        entity.setDateClose(curDate);
        entity.setStatus(PriceBlockStatus.CLOSE);
        entity = refreshInfo(entity);
//        repository.findFirstByStatusOrderByDateOpenDesc(PriceBlockStatus.CLOSE).ifPresent(block ->
//                entity.setAvg(block.getAvg().toString()));
        repository.save(entity);
        eventPublisher.publishEvent(new CandleCloseEvent(entity.getTimeType(), this));
    }

    public void refresh(PriceHistoryBlockEntity block) {
        block = refreshInfo(block);
        block = repository.save(block);

        BigDecimalWrapper currentPrice = binanceClientService.getCurrentPrice();
        if (currentPrice.doubleValue() < BigDecimalWrapper.of(block.getOpen()).doubleValue() * 0.92) {

            log.debug("Big difference price: {}", block);
            eventPublisher.publishEvent(new CreateBuyOrderEvent(this,
                    block,
                    currentPrice,
                    currentPrice.multiply(BigDecimalWrapper.valueOf(1.02)).setScale(2, RoundingMode.HALF_UP).toString()));
        }
    }

    public PriceHistoryBlockEntity refreshInfo(PriceHistoryBlockEntity block) {

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("dateFrom", block.getDateOpen());

        jdbcTemplate.query("select avg(ph.price::decimal) as avg,\n" +
                "       max(ph.price::decimal) as max,\n" +
                "       min(ph.price::decimal) as min,\n" +
                "       (select (ph1.price::decimal)  from cr_schema.price_history ph1 where ph1.date > :dateFrom order by ph1.date limit 1) as open,\n" +
                "       (select (ph2.price::decimal)  from cr_schema.price_history ph2 where ph2.date > :dateFrom order by ph2.date desc limit 1) as close\n" +
                "from cr_schema.price_history ph\n" +
                "where ph.date > :dateFrom", parameterMap, (rs, i) -> {

            block.setAvg(String.valueOf(rs.getDouble("avg")));
            block.setOpen(String.valueOf(rs.getDouble("open")));
            block.setClose(String.valueOf(rs.getDouble("close")));
            block.setMin(String.valueOf(rs.getDouble("min")));
            block.setMax(String.valueOf(rs.getDouble("max")));
            return true;
        });
        return block;
    }
}
