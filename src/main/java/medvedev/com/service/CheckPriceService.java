package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.service.exchangefactory.CryptFiatExchangeStrategy;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.FiatCryptExchangeStrategy;
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

    @Scheduled(cron = "${exchange.cron.check-price}")
    public void checkPrice() {

        TickerStatistics statistics = client.getPriceInfo();
        PriceChangeDto priceChange = priceChangeService.refresh(statistics);

        ExchangeStrategy exchangeStrategy;
        if (priceChange.getState() == PriceChangeState.INCREASED) {
            exchangeStrategy = new FiatCryptExchangeStrategy(balanceCheckerService, client, historyService,
                    systemConfigurationService);
        } else if (priceChange.getState() == PriceChangeState.DECREASED) {
            exchangeStrategy = new CryptFiatExchangeStrategy(client, historyService, systemConfigurationService);
        } else {
            return;
        }

        //exchangeStrategy.launchExchangeAlgorithm(priceChange);
    }
}
