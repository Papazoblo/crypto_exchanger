package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class CryptFiatExchangeStrategy extends BaseExchangeStrategy {

    private static final int PRECISION_SIZE = 4;

    public CryptFiatExchangeStrategy(BinanceApiClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     TelegramPollingService telegramPollingService,
                                     CheckPriceDifferenceService differenceService,
                                     SystemConfigurationService systemConfigurationService) {
        super(binanceClient, historyService, telegramPollingService, differenceService, systemConfigurationService);
    }

    @Override
    public void launchExchangeAlgorithm(PriceHistoryDto priceChange) {
        List<ExchangeHistoryDto> openedExchanges = historyService.getOpenProfitableExchange(priceChange.getPrice());
        List<ExchangeHistoryDto> list = getExchangesWithDifferencePrice(openedExchanges, priceChange.getPrice());
        double sumToExchange = getSumToExchange(list);
        log.info(String.format("Sum to exchange = %s", sumToExchange));
        if (sumToExchange > 0) {
            NewOrderResponse response = sendExchangeRequest(
                    new BigDecimal(sumToExchange).setScale(PRECISION_SIZE, RoundingMode.DOWN), priceChange);
            ExchangeHistoryDto lastExchange = writeToHistory(response, priceChange);
            //historyService.closingOpenedExchangeById(list, lastExchange);
            telegramPollingService.sendMessage(String.format(EXCHANGE_MESSAGE_PATTERN, "ETH => USDT",
                    priceChange.getPrice().toString(),
                    sumToExchange,
                    new BigDecimalWrapper(sumToExchange).multiply(priceChange.getPrice())
                            .setScale(PRECISION_SIZE, RoundingMode.DOWN)));
        }
    }

    @Override
    protected NewOrderResponse sendExchangeRequest(BigDecimal value, PriceHistoryDto priceChange) {
        return null;//binanceClient.createSellOrder(value, priceChange.getPrice().subtract(BigDecimal.ONE).toString());
    }

    private List<ExchangeHistoryDto> getExchangesWithDifferencePrice(List<ExchangeHistoryDto> histories,
                                                                     BigDecimalWrapper lastPrice) {
        return histories.stream()
                .filter(record -> differenceService.isPriceIncreased(lastPrice, record.getPrice().doubleValue()))
                .collect(Collectors.toList());
    }

    private double getSumToExchange(List<ExchangeHistoryDto> histories) {
        double sumOpenedExchange = histories.stream()
                .mapToDouble(record -> record.getInitialAmount().doubleValue())
                .sum();
        double inviolableResidue = systemConfigurationService.findDoubleByName(SystemConfiguration.INVIOLABLE_RESIDUE);
        sumOpenedExchange -= sumOpenedExchange * inviolableResidue;
        return 0;//Double.parseDouble(binanceClient.getBalanceByCurrency(Currency.ETH).getFree()) > sumOpenedExchange
//                ? sumOpenedExchange : 0;
    }
}
