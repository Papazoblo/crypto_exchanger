package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.entity.PriceChangeEntity;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.repository.PriceChangeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceChangeService {

    private final PriceChangeRepository repository;

    public PriceChangeDto refresh(TickerStatistics ticker) {
        PriceChangeEntity priceChangeEntity = repository.findFirstById()
                .map(price -> updatePriceChangeEntity(ticker, price))
                .orElse(createPriceChangeEntity(ticker));
        repository.save(priceChangeEntity);
        return PriceChangeDto.from(priceChangeEntity);
    }

    private static PriceChangeEntity updatePriceChangeEntity(TickerStatistics ticker, PriceChangeEntity entity) {
        entity.setOldPrice(entity.getNewPrice());
        entity.setNewPrice(ticker.getLastPrice());
        if (entity.getOldPriceDecimal().isLessThen(entity.getNewPriceDecimal())) {
            entity.setState(PriceChangeState.INCREASED);
        } else {
            entity.setState(PriceChangeState.DECREASED);
        }
        return entity;
    }

    private static PriceChangeEntity createPriceChangeEntity(TickerStatistics ticker) {
        PriceChangeEntity entity = new PriceChangeEntity();
        entity.setOldPrice(ticker.getLastPrice());
        entity.setNewPrice(ticker.getLastPrice());
        entity.setState(PriceChangeState.WITHOUT_CHANGES);
        return entity;
    }
}
