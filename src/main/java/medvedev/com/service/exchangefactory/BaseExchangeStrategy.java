package medvedev.com.service.exchangefactory;

import medvedev.com.client.BinanceApiClient;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;

public abstract class BaseExchangeStrategy implements ExchangeStrategy {

    protected static final String EXCHANGE_MESSAGE_PATTERN = "*%s*\n_price_: %s\n" +
            "_from_: %s\n_to_: %s";

    protected final BinanceApiClient binanceClient;
    protected final ExchangeHistoryService historyService;
    protected final TelegramPollingService telegramPollingService;
    protected final CheckPriceDifferenceService differenceService;
    protected final SystemConfigurationService systemConfigurationService;

    protected BaseExchangeStrategy(BinanceApiClient binanceClient, ExchangeHistoryService historyService,
                                   TelegramPollingService telegramPollingService,
                                   CheckPriceDifferenceService differenceService,
                                   SystemConfigurationService systemConfigurationService) {
        this.binanceClient = binanceClient;
        this.historyService = historyService;
        this.telegramPollingService = telegramPollingService;
        this.differenceService = differenceService;
        this.systemConfigurationService = systemConfigurationService;
    }


    protected void saveLastPrice(BigDecimalWrapper price) {
        systemConfigurationService.setConfigurationByName(SystemConfiguration.CURRENT_PRICE_LEVEL,
                price.toString());
    }
}
