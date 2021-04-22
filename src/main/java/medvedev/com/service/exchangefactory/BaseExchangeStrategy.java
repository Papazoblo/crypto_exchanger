package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.NewOrderResponse;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.telegram.TelegramPollingService;

import java.math.BigDecimal;

public abstract class BaseExchangeStrategy implements ExchangeStrategy {

    protected final BinanceClient binanceClient;
    protected final ExchangeHistoryService historyService;
    protected final SystemConfigurationService systemConfigurationService;
    protected final TelegramPollingService telegramPollingService;

    protected BaseExchangeStrategy(BinanceClient binanceClient, ExchangeHistoryService historyService,
                                   SystemConfigurationService systemConfigurationService,
                                   TelegramPollingService telegramPollingService) {
        this.binanceClient = binanceClient;
        this.historyService = historyService;
        this.systemConfigurationService = systemConfigurationService;
        this.telegramPollingService = telegramPollingService;
    }

    protected abstract NewOrderResponse sendExchangeRequest(BigDecimal value);

    protected ExchangeHistoryDto writeToHistory(NewOrderResponse response) {
        return historyService.save(ExchangeHistoryEntity.from(response));
    }
}
