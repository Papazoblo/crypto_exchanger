package medvedev.com.service.exchangefactory;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.HavePriceChangeState;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.exception.NoSuitableStrategyException;
import medvedev.com.service.BalanceCheckerService;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.telegram.TelegramPollingService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeStrategyFactory {


    private final ExchangeHistoryService historyService;
    private final BalanceCheckerService balanceCheckerService;
    private final CheckPriceDifferenceService checkPriceDifferenceService;
    private final TelegramPollingService telegramPollingService;
    private final BinanceClient client;

    public ExchangeStrategy getExchangeStrategy(PriceChangeDto priceChange) {

        if (priceChange.getHavePriceChangeState() == HavePriceChangeState.WITH_CHANGES) {
            if (priceChange.getState() == PriceChangeState.INCREASED) {
                return new FiatCryptExchangeStrategy(balanceCheckerService, client, historyService,
                        telegramPollingService, checkPriceDifferenceService);
            } else {
                return new CryptFiatExchangeStrategy(client, historyService, telegramPollingService,
                        checkPriceDifferenceService);
            }
        } else {
            throw new NoSuitableStrategyException();
        }
    }
}