package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.PriceHistoryBlockDto;
import medvedev.com.dto.event.CandleCloseEvent;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.PriceBlockStatus;
import medvedev.com.repository.PriceHistoryBlockRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PriceHistoryBlockService {

    private static final int HISTORY_LIST_SIZE = 10;

    private final PriceHistoryBlockRepository repository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher eventPublisher;


    //фиксирует курс блоками (мин, макс, средний)
    @Scheduled(cron = "${exchange.cron.fixed-price-block}")
    public void createPriceHistoryBlockScheduler() {
        LocalDateTime curDate = LocalDateTime.now();
        getLastBlock(curDate).ifPresent(block -> close(block, curDate));
        create();

//        List<PriceHistoryBlockDto> blockList = getLastBlockList();
//        if (blockList.size() < HISTORY_LIST_SIZE) {
//            log.info("Price history array is empty");
//        } else {
//            exchangerInitializerService.initializeExchangeProcess(blockList);
//        }

        repository.deleteAll(repository.findIdsByOpenDate(LocalDateTime.now().minusMonths(6)));
    }

    public Optional<PriceHistoryBlockEntity> getLastBlock(LocalDateTime curDateTime) {
        return repository.findFirstByDateOpenLessThanAndStatusOrderByDateOpenDesc(curDateTime, PriceBlockStatus.OPEN);
    }

    public Optional<PriceHistoryBlockEntity> getLastOpenedBlock() {
        return repository.findFirstByStatusOrderByDateOpenDesc(PriceBlockStatus.OPEN);
    }

    public List<PriceHistoryBlockEntity> getLast3Block() {
        return repository.getLast3ClosedBlocks();
    }

    public void create() {
        PriceHistoryBlockEntity entity = new PriceHistoryBlockEntity();
        repository.save(entity);
    }

    public void close(PriceHistoryBlockEntity entity, LocalDateTime curDate) {

        Pair<String, String> openClosePriceByBlock = getOpenClosePrice(entity.getId());

        entity.setOpen(openClosePriceByBlock.getFirst());
        entity.setClose(openClosePriceByBlock.getSecond());
        entity.setDateClose(curDate);
        entity.setStatus(PriceBlockStatus.CLOSE);
        repository.findFirstByStatusOrderByDateOpenDesc(PriceBlockStatus.CLOSE).ifPresent(block ->
                entity.setAvg(block.getAvg().toString()));
        repository.save(entity);
        eventPublisher.publishEvent(new CandleCloseEvent(entity.getId(), this));
    }

    public void refresh() {
        List<PriceHistoryBlockEntity> blocks = repository.findAllByStatus(PriceBlockStatus.OPEN);
        blocks.forEach(block -> block.setDateClose(LocalDateTime.now()));
        repository.saveAll(blocks);
    }

    private List<PriceHistoryBlockDto> getLastBlockList() {
        return repository.findAllByDateCloseGreaterThanAndStatusOrderByDateOpenDesc(
                        LocalDateTime.now().minusDays(1L), PriceBlockStatus.CLOSE).stream()
                .limit(PriceHistoryBlockService.HISTORY_LIST_SIZE)
                .map(PriceHistoryBlockDto::of)
                .collect(Collectors.toList());
    }

    public Pair<String, String> getOpenClosePrice(Long blockId) {
        return jdbcTemplate.queryForObject("select\n" +
                        "    (select price\n" +
                        "    from cr_schema.price_history\n" +
                        "    where date = (select min(date) from cr_schema.price_history where history_block_id = :blockId)) as openPrice,\n" +
                        "    (select price\n" +
                        "     from cr_schema.price_history\n" +
                        "     where date = (select max(date) from cr_schema.price_history where history_block_id = :blockId)) as closePrice;\n",
                Map.of("blockId", blockId),
                (rs, num) -> Pair.of(rs.getString("openPrice"), rs.getString("closePrice")));
    }
}
