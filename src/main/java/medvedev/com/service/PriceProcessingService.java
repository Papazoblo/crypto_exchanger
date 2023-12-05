package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.OrderBookResponse;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.ExchangeCancelType;
import medvedev.com.enums.OrderSide;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static medvedev.com.dto.response.OrderBookResponse.PRICE_INDEX;
import static medvedev.com.dto.response.OrderBookResponse.QUANTITY_INDEX;
import static medvedev.com.service.ExchangeService.PRICE_DIFFERENCE;

@Log4j2
@Service
@RequiredArgsConstructor
public class PriceProcessingService {

    @Value("${exchange.price-difference}")
    private Integer priceDifference;
    private final BinanceProperty properties;
    private final BinanceApiClient binanceApiClient;


    public BigDecimalWrapper getCurrentPrice() {
        return new BigDecimalWrapper(binanceApiClient.getCurrentPrice(properties.getSymbol()).getPrice());
    }

    private ExchangeHistoryEntity getFirstOrderBy(ExchangeHistoryEntity order) {
        if (order.getPrevExchange() == null) {
            return order;
        }
        return getFirstOrderBy(order.getPrevExchange());
    }

    public BigDecimalWrapper getPriceToBuy(Optional<ExchangeHistoryEntity> lastOrder) {

        BigDecimalWrapper minExchangePrice;
        if (lastOrder.isPresent() && lastOrder.get().getOperationType() == OrderSide.BUY
                && (lastOrder.get().getCancelType() == null || lastOrder.get().getCancelType() == ExchangeCancelType.BY_PRICE_ADJUSTMENT)) {
            minExchangePrice = new BigDecimalWrapper(getFirstOrderBy(lastOrder.get()).getPrice());
        } else {
            OrderBookResponse response = binanceApiClient.getOrderBook(properties.getSymbol(), 100);
            minExchangePrice = new BigDecimalWrapper(getMaxPriceByQuantity(response.getBids(), OrderSide.BUY).multiply(new BigDecimal("0.9995"))).setScale(2, RoundingMode.HALF_DOWN);
        }


        return lastOrder.filter(item -> item.getOperationType() == OrderSide.BUY)
                .map(item -> {
                    if (item.getCancelType() == ExchangeCancelType.BY_PRICE_ADJUSTMENT) {
                        BigDecimalWrapper resultPrice = new BigDecimalWrapper(new BigDecimalWrapper(lastOrder.get().getPrice()).subtract(PRICE_DIFFERENCE).toString());
                        BigDecimalWrapper currentPrice = getCurrentPrice();
                        if (resultPrice.isGreaterThen(currentPrice)) {
                            return new BigDecimalWrapper(currentPrice.subtract(PRICE_DIFFERENCE));
                        }
                        return resultPrice;
                    } else if (item.getCancelType() == ExchangeCancelType.FROM_SERVICE) {
                        return minExchangePrice;
                    }
                    BigDecimalWrapper newPrice = new BigDecimalWrapper(new BigDecimalWrapper(lastOrder.get().getPrice()).add(PRICE_DIFFERENCE));
                    if (newPrice.isGreaterThen(minExchangePrice)) {
                        return minExchangePrice;
                    }
                    return newPrice;
                })
                .orElse(minExchangePrice);
    }

    public BigDecimalWrapper getPriceToSell(BigDecimal currentPrice, ExchangeHistoryDto lastBuyExchange, Optional<ExchangeHistoryEntity> lastOrder) {

        BigDecimalWrapper minExchangePrice = new BigDecimalWrapper(((lastBuyExchange.getPrice().doubleValue() * lastBuyExchange.getFinalAmount().doubleValue()) + (lastBuyExchange.getFinalAmount().doubleValue() * lastBuyExchange.getPrice().doubleValue() * 0.001) + 0.002) / (lastBuyExchange.getFinalAmount().doubleValue() - 0.001 * lastBuyExchange.getFinalAmount().doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);

        return lastOrder
                .filter(item -> item.getOperationType() == OrderSide.SELL)
                .map(item -> {
                    BigDecimalWrapper lastOrderPrice = new BigDecimalWrapper(item.getPrice());
                    BigDecimalWrapper resultPriceAdd = new BigDecimalWrapper(new BigDecimalWrapper(lastOrder.get().getPrice()).add(PRICE_DIFFERENCE).toString());
                    BigDecimalWrapper resultPriceSubtract = new BigDecimalWrapper(new BigDecimalWrapper(lastOrder.get().getPrice()).subtract(PRICE_DIFFERENCE).toString());
                    log.info("Current: {}", currentPrice);
                    log.info("Last: {}", lastOrderPrice);
                    log.info("Added: {}", resultPriceAdd);
                    log.info("Subtracted: {}", resultPriceSubtract);
                    if (new BigDecimalWrapper(currentPrice).isLessThen(lastOrderPrice)
                            && new BigDecimalWrapper(currentPrice).isGreaterThen(resultPriceSubtract)) {
                        log.info("Branch 1");
                        return resultPriceAdd.setScale(2, RoundingMode.HALF_UP);
                    } else if (new BigDecimalWrapper(currentPrice).isLessThen(lastOrderPrice)
                            && new BigDecimalWrapper(currentPrice).isLessThen(resultPriceSubtract)
                            && resultPriceSubtract.isGreaterThen(minExchangePrice)
                            && item.getCancelType() == ExchangeCancelType.BY_LACK_OF_EXCHANGE) {
                        log.info("Branch 2");
                        return resultPriceSubtract.setScale(2, RoundingMode.HALF_UP);
                    }
                    log.info("Branch 3");
                    return new BigDecimalWrapper(item.getPrice()).setScale(2, RoundingMode.HALF_UP);
                })
                .orElse(minExchangePrice);
    }


    private BigDecimalWrapper getMaxPriceByQuantity(List<String[]> list, OrderSide orderSide) {
        double price = getCurrentPrice().doubleValue();
        Map<Double, List<String[]>> mapPrice = list.stream()
                .collect(groupingBy(item -> Double.valueOf(item[PRICE_INDEX].substring(0, item[PRICE_INDEX].indexOf('.') + 2))));

        Optional<Map.Entry<Double, List<String[]>>> max = mapPrice.entrySet().stream()
                .filter(entry -> Math.abs(entry.getKey() - price) < priceDifference)
                .max((o1, o2) -> Double.compare(o1.getValue().stream()
                        .filter(item -> (orderSide == OrderSide.BUY && Double.parseDouble(item[PRICE_INDEX]) < (price))
                                || (orderSide == OrderSide.SELL && Double.parseDouble(item[PRICE_INDEX]) > price))
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum(), o2.getValue().stream()
                        .filter(item -> (orderSide == OrderSide.BUY && Double.parseDouble(item[PRICE_INDEX]) < (price))
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
