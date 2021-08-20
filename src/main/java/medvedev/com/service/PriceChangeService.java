package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.entity.PriceChangeEntity;
import medvedev.com.enums.HavePriceChangeState;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.repository.PriceChangeRepository;
import medvedev.com.service.telegram.TelegramPollingService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class PriceChangeService {

    private static final String NOTIFY_PRICE_CHANGE_TEMPLATE = "_%s_ => _%s_\n*%s*\n*%s*";

    private final PriceChangeRepository repository;
    private final TelegramPollingService telegramService;

    public PriceChangeDto refresh(TickerStatistics ticker) {
        PriceChangeEntity priceChangeEntity = repository.findFirstById()
                .map(price -> updatePriceChangeEntity(ticker, price))
                .orElse(createPriceChangeEntity(ticker));
        notifyOfChangePrice(priceChangeEntity);
        repository.save(priceChangeEntity);
        return PriceChangeDto.from(priceChangeEntity);
    }

    private void notifyOfChangePrice(PriceChangeEntity priceChangeEntity) {
        telegramService.sendMessage(String.format(NOTIFY_PRICE_CHANGE_TEMPLATE, priceChangeEntity.getOldPrice(),
                priceChangeEntity.getNewPrice(), priceChangeEntity.getState(), priceChangeEntity.getHaveChanges()));
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
        entity.setState(PriceChangeState.INCREASED);
        entity.setHaveChanges(HavePriceChangeState.WITHOUT_CHANGES);
        return entity;
    }
}
