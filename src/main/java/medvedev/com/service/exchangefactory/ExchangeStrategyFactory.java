package medvedev.com.service.exchangefactory;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.exception.NoSuitableStrategyException;
import medvedev.com.service.*;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.service.validator.BuyValidator;
import medvedev.com.service.validator.SellValidator;
import medvedev.com.service.validator.Validator;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ExchangeStrategyFactory {

    private final ExchangeHistoryService historyService;
    private final BalanceCheckerService balanceCheckerService;
    private final CheckPriceDifferenceService checkPriceDifferenceService;
    private final TelegramPollingService telegramPollingService;
    private final BinanceClient client;
    private final SystemConfigurationService systemConfigurationService;
    private final ExchangeConfigDecryptorService configDecryptorService;

    public ExchangeStrategy getExchangeStrategy(PriceHistoryDto[] priceHistory) {

        for (Validator validator : Arrays.asList(new BuyValidator(), new SellValidator())) {
            if (validator.validate(priceHistory, configDecryptorService.getConfig(validator))) {
                if (validator instanceof BuyValidator) {
                    return new FiatCryptExchangeStrategy(balanceCheckerService, client, historyService,
                            telegramPollingService, checkPriceDifferenceService, systemConfigurationService);
                } else {
                    return new CryptFiatExchangeStrategy(client, historyService, telegramPollingService,
                            checkPriceDifferenceService, systemConfigurationService);
                }
            }
        }
        throw new NoSuitableStrategyException();
    }
}
