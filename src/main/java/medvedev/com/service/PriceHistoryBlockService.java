package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.PriceHistoryBlockDto;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.PriceBlockStatus;
import medvedev.com.repository.PriceHistoryBlockRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PriceHistoryBlockService {

    private static final int HISTORY_LIST_SIZE = 10;

    private final PriceHistoryBlockRepository repository;

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

    public void create() {
        PriceHistoryBlockEntity entity = new PriceHistoryBlockEntity();
        repository.save(entity);
    }

    public void close(PriceHistoryBlockEntity entity, LocalDateTime curDate) {
        entity.setDateClose(curDate);
        entity.setStatus(PriceBlockStatus.CLOSE);
        repository.findFirstByStatusOrderByDateOpenDesc(PriceBlockStatus.CLOSE).ifPresent(block ->
                entity.setAvg(block.getAvg().toString()));
        repository.save(entity);
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
}
