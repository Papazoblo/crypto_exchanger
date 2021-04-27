package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.NewOrderResponse;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.telegram.TelegramPollingService;

import java.math.BigDecimal;

public abstract class BaseExchangeStrategy implements ExchangeStrategy {

    protected final BinanceClient binanceClient;
    protected final ExchangeHistoryService historyService;
    protected final TelegramPollingService telegramPollingService;
    protected final CheckPriceDifferenceService differenceService;

    protected BaseExchangeStrategy(BinanceClient binanceClient, ExchangeHistoryService historyService,
                                   TelegramPollingService telegramPollingService,
                                   CheckPriceDifferenceService differenceService) {
        this.binanceClient = binanceClient;
        this.historyService = historyService;
        this.telegramPollingService = telegramPollingService;
        this.differenceService = differenceService;
    }

    protected abstract NewOrderResponse sendExchangeRequest(BigDecimal value, PriceChangeDto priceChange);

    protected ExchangeHistoryDto writeToHistory(NewOrderResponse response, PriceChangeDto priceChange) {
        return historyService.save(ExchangeHistoryEntity.from(response, priceChange));
    }
}
