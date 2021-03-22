package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.AssetBalance;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.enums.Currency;
import medvedev.com.exception.NotEnoughFundsBalanceException;
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

    public FiatCryptExchangeStrategy(BalanceCheckerService balanceCheckerService,
                                     BinanceClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     SystemConfigurationService systemConfigurationService) {
        super(binanceClient, historyService, systemConfigurationService);
        this.balanceCheckerService = balanceCheckerService;
    }

    @Override
    public void launchExchangeAlgorithm() {
        AssetBalance balance = binanceClient.getBalanceByCurrency(Currency.USDT);
        try {
            BigDecimalWrapper exchangeAmount = balanceCheckerService.isEnoughFundsBalance(balance.getFree());
            doExchange(exchangeAmount);
        } catch (NotEnoughFundsBalanceException ex) {
            System.out.print(ex.getMessage());
        }
    }

    @Override
    protected void sendExchangeRequest(BigDecimal value) {
        binanceClient.creteBuyOrder(value);
    }

    private void doExchange(BigDecimalWrapper value) {
        List<ExchangeHistoryDto> historyList = historyService.findLastExchangeFiatCryptInTimeRange();
        if (!historyList.isEmpty()) {
            if (getLastMinRate(historyList).isGreaterThen(getLastPrice())) {
                sendExchangeRequest(value);
            } else {
                //TODO отправляем запрос на обмен в телелграм
            }
        } else {
            sendExchangeRequest(value);
        }
    }

    private static BigDecimalWrapper getLastMinRate(List<ExchangeHistoryDto> historyList) {
        return historyList.stream()
                .min(Comparator.comparing(ExchangeHistoryDto::getPrice))
                .map(ExchangeHistoryDto::getPrice)
                .orElse(new BigDecimalWrapper(0));
    }
}
