package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.AssetBalance;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.BalanceCheckerService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class FiatCryptExchangeStrategy extends BaseExchangeStrategy {

    private final BalanceCheckerService balanceCheckerService;
    private final ExchangeHistoryService historyService;

    public FiatCryptExchangeStrategy(BalanceCheckerService balanceCheckerService,
                                     BinanceClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     SystemConfigurationService systemConfigurationService) {
        super(binanceClient, historyService, systemConfigurationService);
        this.balanceCheckerService = balanceCheckerService;
        this.historyService = historyService;
    }

    @Override
    public void launchExchangeAlgorithm(PriceChangeDto priceChange) {
        AssetBalance balance = binanceClient.getBalanceByCurrency(Currency.USDT);
        BigDecimalWrapper exchangeAmount = balanceCheckerService.isEnoughFundsBalance(balance.getFree());

        if (historyService.getExchangeCount() > 0) {
            doExchange(exchangeAmount, priceChange);
        } else {
            sendExchangeRequest(exchangeAmount);
        }
    }

    @Override
    protected void sendExchangeRequest(BigDecimal value) {
        binanceClient.creteBuyOrder(value);
    }

    private void doExchange(BigDecimalWrapper value, PriceChangeDto priceChange) {
        List<ExchangeHistoryDto> historyList = historyService.findLastExchangeFiatCryptInTimeRange();
        double priceDifference = systemConfigurationService.findDoubleByName(
                SystemConfiguration.MIN_DIFFERENCE_PRICE_FIAT_CRYPT);
        if (!historyList.isEmpty()) {
            BigDecimalWrapper lastMinPrice = getLastMinPrice(historyList);
            if (isDifference(priceChange.getNewPrice(), lastMinPrice.doubleValue(), priceDifference)) {
                sendExchangeRequest(value);
            }
        } else {
            historyService.getLastFiatCrypt().ifPresent(dto -> {
                if (isDifference(priceChange.getNewPrice(), dto.getPrice().doubleValue(), priceDifference)) {
                    sendExchangeRequest(value);
                }
            });
        }
    }

    private boolean isDifference(BigDecimalWrapper lastPrice, double recordPrice, double priceDifference) {
        double lastPriceInDouble = lastPrice.doubleValue();
        return -((recordPrice * 100 / lastPriceInDouble) - 100) < priceDifference;
    }

    private static BigDecimalWrapper getLastMinPrice(List<ExchangeHistoryDto> historyList) {
        return historyList.stream()
                .min(Comparator.comparing(ExchangeHistoryDto::getPrice))
                .map(ExchangeHistoryDto::getPrice)
                .orElse(new BigDecimalWrapper(0));
    }
}
