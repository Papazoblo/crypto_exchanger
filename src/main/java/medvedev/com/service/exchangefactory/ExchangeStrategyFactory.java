package medvedev.com.service.exchangefactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.PriceHistoryBlockDto;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.exception.NoSuitableStrategyException;
import medvedev.com.service.*;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.service.validator.BuyValidator;
import medvedev.com.service.validator.SellValidator;
import medvedev.com.service.validator.Validator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class ExchangeStrategyFactory {

    private final ExchangeHistoryService historyService;
    private final BalanceCheckerService balanceCheckerService;
    private final CheckPriceDifferenceService checkPriceDifferenceService;
    private final TelegramPollingService telegramPollingService;
    private final BinanceApiClient client;
    private final SystemConfigurationService systemConfigurationService;
    private final ExchangeConfigDecryptorService configDecryptorService;

    public ExchangeStrategy getExchangeStrategy(List<PriceHistoryBlockDto> priceBlocksHistory) {

        for (Validator validator : Arrays.asList(new BuyValidator(), new SellValidator())) {
            List<PriceChangeState[]> configList = configDecryptorService.getConfig(validator);
            Optional<PriceChangeState[]> result = validator.validate(priceBlocksHistory, configList);
            if (result.isEmpty()) {
                continue;
            }

            log.info("Configuration find match " + Arrays.stream(result.get()).map(Enum::name)
                    .collect(Collectors.joining(",")));
            if (validator instanceof BuyValidator) {
                log.info("Fiat -> Crypt");
//                return new FiatCryptExchangeStrategy(balanceCheckerService, client, historyService,
//                        telegramPollingService, checkPriceDifferenceService, systemConfigurationService);
            } else {
//                log.info("Crypt -> Fiat");
//                return new CryptFiatExchangeStrategy(client, historyService, telegramPollingService,
//                        checkPriceDifferenceService, systemConfigurationService);
            }
        }
        throw new NoSuitableStrategyException();
    }
}
