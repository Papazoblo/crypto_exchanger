package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.entity.PriceHistoryEntity;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.repository.PriceHistoryRepository;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository repository;
    private final TimeService timeService;

    public void savePrice(BigDecimalWrapper newPrice) {
        PriceHistoryEntity entity = new PriceHistoryEntity();
        entity.setDate(timeService.now());
        entity.setPrice(newPrice.toString());
        getLastPrice().ifPresent(price -> entity.setChangeState(processChangeState(price.getPrice(), newPrice)));
        repository.save(entity);
    }

    public Optional<PriceHistoryEntity> getLastPrice() {
        return repository.findFirstByDateGreaterThanOrderByDateDesc(timeService.nowMinusMinutes(60));
    }

    public PriceHistoryDto[] getFirstRecords(int count) {
        return repository.findAllByDateGreaterThanOrderByDateDesc(timeService.nowMinusMinutes(60)).stream()
                .map(PriceHistoryDto::of)
                .collect(toList())
                .subList(0, count)
                .toArray(new PriceHistoryDto[count]);
    }

    private PriceChangeState processChangeState(BigDecimalWrapper from, BigDecimalWrapper to) {
        if (from.isLessThen(to)) {
            return PriceChangeState.INCREASED;
        } else {
            return PriceChangeState.DECREASED;
        }
    }
}
