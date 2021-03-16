package medvedev.com.service;

import com.binance.api.client.domain.OrderSide;
import lombok.RequiredArgsConstructor;
import medvedev.com.service.exchangefactory.ExchangeStrategyFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangerService {

    /**
     * 1. Нужно получить баланс
     * 2. Хватает ли нам баланса для обмена fiat-crypt
     * 3. Если хватает, то меняем и переходим к 3.2
     * 3.2 Если не хватает, то ищем по истории обмен crypt-fiat
     * 4. Берем текущий курс
     * <p>
     * 6. Меняем и засыпаем
     */

    private final ExchangeStrategyFactory strategyFactory;

    @Scheduled(cron = "")
    public void launchExchange() {
        //TODO лог начала обмена фиат крипта
        strategyFactory.getStrategy(OrderSide.BUY).launchExchangeAlgorithm();
        //TODO лог начала обмена крипта фиат
        strategyFactory.getStrategy(OrderSide.SELL).launchExchangeAlgorithm();
        //TODO лог окончания обмена
    }
}
