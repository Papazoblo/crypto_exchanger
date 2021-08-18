package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
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

    public CryptFiatExchangeStrategy(BinanceClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     TelegramPollingService telegramPollingService,
                                     CheckPriceDifferenceService differenceService,
                                     SystemConfigurationService systemConfigurationService) {
        super(binanceClient, historyService, telegramPollingService, differenceService, systemConfigurationService);
    }

    /**
     * 1. Достаем из базы список открытых обменов
     * 2. Сравниваем курс (что бы был больше и прибыль была минимум N% от затраченной суммы)
     * 3. Делаем обмен
     */


    @Override
    public void launchExchangeAlgorithm(PriceChangeDto priceChange) {
        //получили список открытых обменов у которых курс обмена МЕНЬШЕ ТЕКУЩЕГО
        List<ExchangeHistoryDto> openedExchanges = historyService.getOpenProfitableExchange(priceChange.getNewPrice());
        //Выбираем записи у которых текущий курс БОЛЬШЕ курса обмена на N %
        List<ExchangeHistoryDto> list = getExchangesWithDifferencePrice(openedExchanges, priceChange.getNewPrice());
        double sumToExchange = getSumToExchange(list);
        if (!list.isEmpty() && sumToExchange > 0) {
            NewOrderResponse response = sendExchangeRequest(
                    new BigDecimal(sumToExchange).setScale(PRECISION_SIZE, RoundingMode.DOWN), priceChange);
            ExchangeHistoryDto lastExchange = writeToHistory(response, priceChange);
            historyService.closingOpenedExchangeById(list, lastExchange);
            telegramPollingService.sendMessage(String.format(EXCHANGE_MESSAGE_PATTERN, "ETH => USDT",
                    priceChange.getNewPrice().toString(),
                    sumToExchange,
                    new BigDecimalWrapper(sumToExchange).multiply(priceChange.getNewPrice())
                            .setScale(PRECISION_SIZE, RoundingMode.DOWN)));
        }
    }

    @Override
    protected NewOrderResponse sendExchangeRequest(BigDecimal value, PriceChangeDto priceChange) {
        log.info("Start sell exchange: " + value.toString() + " ETH");
        return binanceClient.createSellOrder(value);
    }

    private List<ExchangeHistoryDto> getExchangesWithDifferencePrice(List<ExchangeHistoryDto> histories,
                                                                     BigDecimalWrapper lastPrice) {
        return histories.stream()
                .filter(record -> differenceService.isPriceIncreased(lastPrice, record.getPrice().doubleValue()))
                .collect(Collectors.toList());
    }

    private double getSumToExchange(List<ExchangeHistoryDto> histories) {
        double sumOpenedExchange = histories.stream()
                .mapToDouble(record -> record.getFinalAmount().doubleValue())
                .sum();
        double inviolableResidue = systemConfigurationService.findDoubleByName(SystemConfiguration.INVIOLABLE_RESIDUE);
        sumOpenedExchange -= sumOpenedExchange * inviolableResidue;
        return Double.parseDouble(binanceClient.getBalanceByCurrency(Currency.ETH).getFree()) > sumOpenedExchange
                ? sumOpenedExchange : 0;
    }
}
