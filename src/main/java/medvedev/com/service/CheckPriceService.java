package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.HavePriceChangeState;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.FiatCryptExchangeStrategy;
import medvedev.com.service.telegram.TelegramPollingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckPriceService {

    private final BinanceClient client;
    private final PriceChangeService priceChangeService;
    private final ExchangeHistoryService historyService;
    private final BalanceCheckerService balanceCheckerService;
    private final SystemConfigurationService systemConfigurationService;
    private final SystemStateService stateService;
    private final TelegramPollingService telegramPollingService;

    @Scheduled(cron = "${exchange.cron.check-price}")
    public void checkPrice() {

        TickerStatistics statistics = client.getPriceInfo();
        PriceChangeDto priceChange = priceChangeService.refresh(statistics);

        if (stateService.isSystemNotLaunched()) {
            return;
        }

        ExchangeStrategy exchangeStrategy;
        if (priceChange.getHavePriceChangeState() == HavePriceChangeState.WITH_CHANGES) {
            if (priceChange.getState() == PriceChangeState.INCREASED) {
                exchangeStrategy = new FiatCryptExchangeStrategy(balanceCheckerService, client, historyService,
                        systemConfigurationService, telegramPollingService);

                exchangeStrategy.launchExchangeAlgorithm(priceChange);
            } else {
                //exchangeStrategy = new CryptFiatExchangeStrategy(client, historyService, systemConfigurationService);
            }
        } else {
            return;
        }

    }
}
