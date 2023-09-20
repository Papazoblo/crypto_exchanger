package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.dto.SystemConfigurationDto;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.service.BalanceCheckerService;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Log4j2
public class FiatCryptExchangeStrategy extends BaseExchangeStrategy {

    private static final int PRECISION_SIZE = 4;
    private static final BigDecimalWrapper MIN_AMOUNT_TO_EXCHANGE = new BigDecimalWrapper(0.01);

    private final BalanceCheckerService balanceCheckerService;
    private final ExchangeHistoryService historyService;

    public FiatCryptExchangeStrategy(BalanceCheckerService balanceCheckerService,
                                     BinanceApiClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     TelegramPollingService telegramPollingService,
                                     CheckPriceDifferenceService differenceService,
                                     SystemConfigurationService systemConfigurationService) {
        super(binanceClient, historyService, telegramPollingService, differenceService, systemConfigurationService);
        this.balanceCheckerService = balanceCheckerService;
        this.historyService = historyService;
    }

    @Override
    public void launchExchangeAlgorithm(PriceHistoryDto priceHistory) {
//        AssetBalance balance = binanceClient.getBalanceByCurrency(Currency.USDT);
//        BigDecimalWrapper exchangeAmount = balanceCheckerService.getAmountToExchange(balance.getFree());
//        BigDecimalWrapper convertedValue = convertFiatToCrypt(exchangeAmount, priceHistory.getPrice());
//
//        if (historyService.isExistExchangeSell()) {
//            doExchange(convertedValue, priceHistory);
//        } else {
//            sendExchangeRequest(convertedValue, priceHistory);
//        }
    }

    @Override
    protected NewOrderResponse sendExchangeRequest(BigDecimal value, PriceHistoryDto priceHistory) {
//        NewOrderResponse response = binanceClient.createBuyOrder(value, priceHistory.getPrice().toString());
//        writeToHistory(response, priceHistory);
//        telegramPollingService.sendMessage(String.format(EXCHANGE_MESSAGE_PATTERN, "USDT => ETH",
//                priceHistory.getPrice().toString(),
//                value.toString(),
//                value.multiply(priceHistory.getPrice())));
//        return response;
        return null;
    }

    private void doExchange(BigDecimalWrapper amount, PriceHistoryDto priceHistory) {
        double lastSellPrice;
        try {
            lastSellPrice = systemConfigurationService.findDoubleByName(SystemConfiguration.CURRENT_PRICE_LEVEL);
        } catch (EntityNotFoundException ex) {
            systemConfigurationService.save(new SystemConfigurationDto(SystemConfiguration.CURRENT_PRICE_LEVEL.name(),
                    priceHistory.getPrice().toString()));
            throw ex;
        }

        if (differenceService.isPriceDecreased(priceHistory.getPrice(), lastSellPrice)) {
            sendExchangeRequest(amount, priceHistory);
        }
    }

    private BigDecimalWrapper convertFiatToCrypt(BigDecimalWrapper value, BigDecimalWrapper price) {
        BigDecimalWrapper amount = new BigDecimalWrapper(value.divide(price, PRECISION_SIZE, RoundingMode.DOWN)
                .toString());
        if (amount.isLessThenOrEqual(MIN_AMOUNT_TO_EXCHANGE)) {
            log.info("Not enough funds on balance");
        }
        return amount;
    }
}
