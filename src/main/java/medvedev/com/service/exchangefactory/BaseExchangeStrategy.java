package medvedev.com.service.exchangefactory;

import medvedev.com.client.BinanceClient;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;

import java.math.BigDecimal;

public abstract class BaseExchangeStrategy implements ExchangeStrategy {

    protected final BinanceClient binanceClient;
    protected final ExchangeHistoryService historyService;
    protected final SystemConfigurationService systemConfigurationService;

    protected BaseExchangeStrategy(BinanceClient binanceClient, ExchangeHistoryService historyService,
                                   SystemConfigurationService systemConfigurationService) {
        this.binanceClient = binanceClient;
        this.historyService = historyService;
        this.systemConfigurationService = systemConfigurationService;
    }

    protected abstract void sendExchangeRequest(BigDecimal value);
}
