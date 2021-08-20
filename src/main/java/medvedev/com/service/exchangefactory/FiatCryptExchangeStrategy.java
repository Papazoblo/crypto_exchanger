package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
import medvedev.com.enums.SystemConfiguration;
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
                                     BinanceClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     TelegramPollingService telegramPollingService,
                                     CheckPriceDifferenceService differenceService,
                                     SystemConfigurationService systemConfigurationService) {
        super(binanceClient, historyService, telegramPollingService, differenceService, systemConfigurationService);
        this.balanceCheckerService = balanceCheckerService;
        this.historyService = historyService;
    }

    @Override
    public void launchExchangeAlgorithm(PriceChangeDto priceChange) {
        AssetBalance balance = binanceClient.getBalanceByCurrency(Currency.USDT);
        BigDecimalWrapper exchangeAmount = balanceCheckerService.getAmountToExchange(balance.getFree());
        BigDecimalWrapper convertedValue = convertFiatToCrypt(exchangeAmount, priceChange.getNewPrice());

        if (historyService.isExistExchangeSell()) {
            doExchange(convertedValue, priceChange);
        } else {
            sendExchangeRequest(convertedValue, priceChange);
        }
    }

    @Override
    protected NewOrderResponse sendExchangeRequest(BigDecimal value, PriceChangeDto priceChange) {
        NewOrderResponse response = binanceClient.createBuyOrder(value);
        writeToHistory(response, priceChange);
        telegramPollingService.sendMessage(String.format(EXCHANGE_MESSAGE_PATTERN, "USDT => ETH",
                priceChange.getNewPrice().toString(),
                value.toString(),
                value.multiply(priceChange.getNewPrice())));
        return response;
    }

    private void doExchange(BigDecimalWrapper amount, PriceChangeDto priceChange) {
        double lastSellPrice = systemConfigurationService.findDoubleByName(SystemConfiguration.CURRENT_PRICE_LEVEL);

        if (differenceService.isPriceDecreased(priceChange.getNewPrice(), lastSellPrice)) {
            sendExchangeRequest(amount, priceChange);
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
