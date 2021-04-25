package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
import medvedev.com.service.BalanceCheckerService;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Log4j2
public class FiatCryptExchangeStrategy extends BaseExchangeStrategy {

    private static final int PRECISION_SIZE = 8;
    private static final BigDecimalWrapper MIN_AMOUNT_TO_EXCHANGE = new BigDecimalWrapper(0.01);

    private final BalanceCheckerService balanceCheckerService;
    private final ExchangeHistoryService historyService;

    public FiatCryptExchangeStrategy(BalanceCheckerService balanceCheckerService,
                                     BinanceClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     TelegramPollingService telegramPollingService,
                                     CheckPriceDifferenceService differenceService) {
        super(binanceClient, historyService, telegramPollingService,
                differenceService);
        this.balanceCheckerService = balanceCheckerService;
        this.historyService = historyService;
    }

    @Override
    public void launchExchangeAlgorithm(PriceChangeDto priceChange) {
        AssetBalance balance = binanceClient.getBalanceByCurrency(Currency.USDT);
        BigDecimalWrapper exchangeAmount = balanceCheckerService.isEnoughFundsBalance(balance.getFree());
        BigDecimalWrapper convertedValue = convertFiatToCrypt(exchangeAmount, priceChange.getNewPrice());

        if (convertedValue.isLessThenOrEqual(MIN_AMOUNT_TO_EXCHANGE)) {
            log.info("Not enough funds on balance");
            return;
        }

        if (historyService.isNotExistExchangeSell()) {
            sendExchangeRequest(convertedValue);
        } else {
            doExchange(convertedValue, priceChange);
        }
    }

    @Override
    protected NewOrderResponse sendExchangeRequest(BigDecimal value) {
        log.info("Start buy exchange: " + value.toString() + " ETH");
        NewOrderResponse response = binanceClient.createSellOrder(value);
        writeToHistory(response);
        telegramPollingService.sendMessage(String.format("Launch exchange USDT => ETH: amount = %s", value.toString()));
        return response;
    }

    private BigDecimalWrapper convertFiatToCrypt(BigDecimalWrapper value, BigDecimalWrapper price) {
        return new BigDecimalWrapper(value.divide(price, PRECISION_SIZE, RoundingMode.DOWN).toString());
    }

    private void doExchange(BigDecimalWrapper amount, PriceChangeDto priceChange) {
        //берем последний обмен КРИПТА ФИАТ
        ExchangeHistoryDto lastSellExchange = historyService.findLastSellExchange();

        if (differenceService.isPriceDecreased(priceChange.getNewPrice(), lastSellExchange.getPrice().doubleValue())) {
            sendExchangeRequest(amount);
        }
    }

}
