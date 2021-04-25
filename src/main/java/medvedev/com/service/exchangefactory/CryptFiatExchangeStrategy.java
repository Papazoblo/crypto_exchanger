package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class CryptFiatExchangeStrategy extends BaseExchangeStrategy {

    public CryptFiatExchangeStrategy(BinanceClient binanceClient,
                                     ExchangeHistoryService historyService,
                                     TelegramPollingService telegramPollingService,
                                     CheckPriceDifferenceService differenceService) {
        super(binanceClient, historyService, telegramPollingService, differenceService);
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
            NewOrderResponse response = sendExchangeRequest(new BigDecimal(sumToExchange));
            ExchangeHistoryDto lastExchange = writeToHistory(response);
            historyService.closingOpenedExchangeById(list, lastExchange);
            telegramPollingService.sendMessage(String.format("Launch exchange ETH => USDT: amount = %s",
                    sumToExchange));
        }
    }

    @Override
    protected NewOrderResponse sendExchangeRequest(BigDecimal value) {
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
        sumOpenedExchange -= sumOpenedExchange * 0.02;
        return Double.parseDouble(binanceClient.getBalanceByCurrency(Currency.ETH).getFree()) > sumOpenedExchange
                ? sumOpenedExchange : 0;
    }
}
