package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.entity.PriceHistoryEntity;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.repository.PriceHistoryRepository;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository repository;
    private final PriceHistoryBlockService priceHistoryBlockService;
    private final TimeService timeService;

    public void savePrice(BigDecimalWrapper newPrice) {
        PriceHistoryEntity entity = new PriceHistoryEntity();
        entity.setDate(timeService.now());
        entity.setPrice(newPrice.toString());
        getLastPrice().ifPresent(price -> entity.setChangeState(processChangeState(price.getPrice(), newPrice)));
        priceHistoryBlockService.getLastBlock(entity.getDate()).ifPresent(block -> {
            entity.setHistoryBlock(block);
            repository.save(entity);
            priceHistoryBlockService.refresh();
        });
    }

    public Optional<PriceHistoryEntity> getLastPrice() {
        return repository.findFirstByDateGreaterThanOrderByDateDesc(timeService.nowMinusMinutes(60));
    }

    private PriceChangeState processChangeState(BigDecimalWrapper from, BigDecimalWrapper to) {
        if (from.isLessThen(to)) {
            return PriceChangeState.INCREASED;
        } else {
            return PriceChangeState.DECREASED;
        }
    }
}
