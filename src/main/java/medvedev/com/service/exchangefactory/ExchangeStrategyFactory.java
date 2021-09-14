package medvedev.com.service.exchangefactory;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.CheckPriceType;
import medvedev.com.enums.HavePriceChangeState;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.exception.NoSuitableStrategyException;
import medvedev.com.service.*;
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
    private final SystemConfigurationService systemConfigurationService;
    private final TimeService timeService;

    public ExchangeStrategy getExchangeStrategy(PriceChangeDto priceChangeOne, PriceChangeDto priceChangeTwo,
                                                CheckPriceType checkPriceType) {

        if ((priceChangeOne.getHavePriceChangeState() == HavePriceChangeState.WITH_CHANGES &&
                priceChangeTwo.getHavePriceChangeState() == HavePriceChangeState.WITHOUT_CHANGES) ||
                (priceChangeOne.getHavePriceChangeState() == HavePriceChangeState.WITHOUT_CHANGES &&
                        priceChangeTwo.getHavePriceChangeState() == HavePriceChangeState.WITHOUT_CHANGES) ||
                priceChangeTwo.getHavePriceChangeState() == HavePriceChangeState.WITH_CHANGES) {
            if (priceChangeTwo.getState() == PriceChangeState.INCREASED && checkPriceType == CheckPriceType.NORMAL) {
                return new FiatCryptExchangeStrategy(balanceCheckerService, client, historyService,
                        telegramPollingService, checkPriceDifferenceService, systemConfigurationService);
            } else {
                if (isCheckTypeValid(checkPriceType, priceChangeTwo)) {
                    return new CryptFiatExchangeStrategy(client, historyService, telegramPollingService,
                            checkPriceDifferenceService, systemConfigurationService);
                }
            }
        }
        throw new NoSuitableStrategyException();
    }

    private boolean isCheckTypeValid(CheckPriceType checkPriceType, PriceChangeDto priceChangeDto) {
        if (checkPriceType == CheckPriceType.NORMAL) {
            return true;
        }
        return historyService.getOpenProfitableExchange(priceChangeDto.getNewPrice()).stream()
                .anyMatch(exchange -> exchange.getDateTime().plusHours(12).isBefore(timeService.now()));


    }
}
