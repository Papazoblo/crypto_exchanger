package medvedev.com.service.exchangefactory;

import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.enums.Currency;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.wrapper.BigDecimalWrapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CryptFiatExchangeStrategy extends BaseExchangeStrategy {

    public CryptFiatExchangeStrategy(BinanceClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     SystemConfigurationService systemConfigurationService) {
        super(binanceClient, historyService, systemConfigurationService);
    }

    /**
     * 1. Берем текущий курс.
     * 2. Делаем поиск открытых обменов
     * 3. Сравниваем курс (что бы был больше и прибыль была минимум N% от затраченной суммы)
     * 4. Делаем обмен
     */


    @Override
    public void launchExchangeAlgorithm() {
        BigDecimalWrapper lastPrice = getLastPrice();
        List<ExchangeHistoryDto> list = getExchangesWithDifferencePrice(
                historyService.getOpenProfitableExchange(lastPrice), lastPrice);
        double sumToExchange = getSumToExchange(list);
        if (!list.isEmpty() && sumToExchange > 0) {
            sendExchangeRequest(new BigDecimal(sumToExchange));
        }
    }

    @Override
    protected void sendExchangeRequest(BigDecimal value) {
        binanceClient.createSellOrder(value);
    }

    //TODO надо проверить фильтр по курсу
    private List<ExchangeHistoryDto> getExchangesWithDifferencePrice(List<ExchangeHistoryDto> histories,
                                                                     BigDecimalWrapper lastPrice) {
        double priceDifference = systemConfigurationService.findDoubleByName(SystemConfiguration.MIN_DIFFERENCE_PRICE);
        return histories.stream()
                .filter(record -> isDifference(lastPrice, record.getPrice().doubleValue(),
                        priceDifference))
                .collect(Collectors.toList());
    }

    private boolean isDifference(BigDecimalWrapper lastPrice, double recordPrice, double priceDifference) {
        double lastPriceInDouble = lastPrice.doubleValue();
        return (recordPrice * 100 / lastPriceInDouble) - 100 > priceDifference;
    }

    private double getSumToExchange(List<ExchangeHistoryDto> histories) {
        double sumOpenedExchange = histories.stream()
                .mapToDouble(record -> record.getFinalAmount().doubleValue())
                .sum();
        return Double.parseDouble(binanceClient.getBalanceByCurrency(Currency.ETH).getFree()) > sumOpenedExchange
                ? sumOpenedExchange : 0;
    }
}
