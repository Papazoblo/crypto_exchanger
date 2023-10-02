package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.GetCurrentPriceResponse;
import medvedev.com.dto.response.OrderBookResponse;
import medvedev.com.enums.OrderSide;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static medvedev.com.dto.response.OrderBookResponse.PRICE_INDEX;
import static medvedev.com.dto.response.OrderBookResponse.QUANTITY_INDEX;

@Log4j2
@Service
@RequiredArgsConstructor
public class PriceProcessingService {

    @Value("${exchange.price-difference}")
    private Integer priceDifference;

    private final BinanceApiClient client;
    private final BinanceProperty properties;
    private final PriceHistoryService priceHistoryService;
    private final BinanceApiClient binanceApiClient;

    //фиксация текущего курса и обновление блока
    @Scheduled(fixedRateString = "${fixed-rate.check-price}")
    public void checkPriceNormal() {
        GetCurrentPriceResponse response = client.getCurrentPrice(properties.getSymbol());
        priceHistoryService.savePrice(BigDecimalWrapper.of(response.getPrice()));
    }

    public BigDecimalWrapper getPriceToBuy() {
        OrderBookResponse response = binanceApiClient.getOrderBook(properties.getSymbol(), 1000);
        return getMaxPriceByQuantity(response.getBids(), OrderSide.BUY);
    }

    public BigDecimalWrapper getPriceToSell() {
        OrderBookResponse response = binanceApiClient.getOrderBook(properties.getSymbol(), 1000);
        return getMaxPriceByQuantity(response.getAsks(), OrderSide.SELL);
    }


    private BigDecimalWrapper getMaxPriceByQuantity(List<String[]> list, OrderSide orderSide) {
        double price = Double.parseDouble(binanceApiClient.getCurrentPrice(properties.getSymbol()).getPrice());
        Map<Integer, List<String[]>> mapPrice = list.stream()
                .collect(groupingBy(item -> Integer.valueOf(item[PRICE_INDEX].substring(0, item[PRICE_INDEX].indexOf('.')))));

        Optional<Map.Entry<Integer, List<String[]>>> max = mapPrice.entrySet().stream()
                .filter(entry -> Math.abs(entry.getKey() - price) < priceDifference)
                .max((o1, o2) -> Double.compare(o1.getValue().stream()
                        .filter(item -> (orderSide == OrderSide.BUY && Double.parseDouble(item[PRICE_INDEX]) < price)
                                || (orderSide == OrderSide.SELL && Double.parseDouble(item[PRICE_INDEX]) > price))
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum(), o2.getValue().stream()
                        .filter(item -> (orderSide == OrderSide.BUY && Double.parseDouble(item[PRICE_INDEX]) < price)
                                || (orderSide == OrderSide.SELL && Double.parseDouble(item[PRICE_INDEX]) > price))
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum()));

        return max.map(entry -> entry.getValue().stream()
                        .max(Comparator.comparing(o -> Double.valueOf(o[QUANTITY_INDEX])))
                        .map(value -> new BigDecimalWrapper(value[PRICE_INDEX]))
                        .orElse(new BigDecimalWrapper(price)))
                .orElse(new BigDecimalWrapper(price));
    }
}
