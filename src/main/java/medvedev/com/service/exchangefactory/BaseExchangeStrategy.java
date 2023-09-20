package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.NewOrderResponse;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;

import java.math.BigDecimal;

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

    protected abstract NewOrderResponse sendExchangeRequest(BigDecimal value, PriceHistoryDto priceHistory);

    protected ExchangeHistoryDto writeToHistory(NewOrderResponse response, PriceHistoryDto priceHistory) {
        saveLastPrice(priceHistory.getPrice());
        return historyService.save(ExchangeHistoryEntity.from(response, priceHistory));
    }

    protected void saveLastPrice(BigDecimalWrapper price) {
        systemConfigurationService.setConfigurationByName(SystemConfiguration.CURRENT_PRICE_LEVEL,
                price.toString());
    }
}
