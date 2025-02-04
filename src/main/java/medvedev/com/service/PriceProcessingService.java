package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.component.ExchangeProperties;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.OrderBookResponse;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.ExchangeCancelType;
import medvedev.com.enums.OrderSide;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.scheduling.annotation.Scheduled;
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

@Log4j2
@Service
@RequiredArgsConstructor
public class PriceProcessingService {

    private final BinanceProperty properties;
    private final BinanceApiClient binanceApiClient;
    private final ExchangeProperties exchangeProperties;
    private final PriceHistoryService priceHistoryService;


    @Scheduled(cron = "${exchange.cron.every-3-sec}")
    public BigDecimalWrapper getCurrentPrice() {
        BigDecimalWrapper currentPrice = new BigDecimalWrapper(binanceApiClient.getCurrentPrice(properties.getSymbol()).getPrice());
        priceHistoryService.savePrice(currentPrice);
        return currentPrice;
    }

    private ExchangeHistoryEntity getFirstOrderBy(ExchangeHistoryEntity order) {
        if (order.getPrevExchange() == null) {
            return order;
        }
        return getFirstOrderBy(order.getPrevExchange());
    }

    public BigDecimalWrapper getPriceToBuy() {
        OrderBookResponse response = binanceApiClient.getOrderBook(properties.getSymbol(), 150);
        return new BigDecimalWrapper(getMaxPriceByQuantity(response.getBids(), OrderSide.BUY).multiply(new BigDecimal("0.9999"))).setScale(2, RoundingMode.DOWN);
    }

    public BigDecimalWrapper getPriceToBuy(Optional<ExchangeHistoryEntity> lastOrder) {

        BigDecimalWrapper minPrice;
        if (lastOrder.isPresent() && lastOrder.get().getOperationType() == OrderSide.BUY
                && (lastOrder.get().getCancelType() == null || lastOrder.get().getCancelType() == ExchangeCancelType.BY_PRICE_ADJUSTMENT)) {
            minPrice = new BigDecimalWrapper(getFirstOrderBy(lastOrder.get()).getPrice());
        } else {
            OrderBookResponse response = binanceApiClient.getOrderBook(properties.getSymbol(), 150);
            minPrice = new BigDecimalWrapper(getMaxPriceByQuantity(response.getBids(), OrderSide.BUY).multiply(new BigDecimal("0.9998"))).setScale(2, RoundingMode.DOWN);
        }

        BigDecimalWrapper currentPrice = getCurrentPrice();
        BigDecimalWrapper minExchangePrice;
        if (currentPrice.isLessThenOrEqual(minPrice)) {
            minExchangePrice = new BigDecimalWrapper(currentPrice.subtract(BigDecimal.valueOf(exchangeProperties.getPriceDifference())));
        } else {
            minExchangePrice = minPrice;
        }

        return lastOrder.filter(item -> item.getOperationType() == OrderSide.BUY)
                .map(item -> {
                    if (item.getCancelType() == ExchangeCancelType.BY_PRICE_ADJUSTMENT) {
                        BigDecimalWrapper resultPriceSimple = processingPrice(lastOrder.get().getPrice(), BigDecimal.valueOf(exchangeProperties.getPriceDifference()), false);
                        BigDecimalWrapper resultPriceDouble = processingPrice(lastOrder.get().getPrice(), BigDecimal.valueOf(exchangeProperties.getDoublePriceDifference()), false);
                        if (resultPriceSimple.isGreaterThen(currentPrice)) {
                            if (resultPriceDouble.isGreaterThen(currentPrice)) {
                                return new BigDecimalWrapper(currentPrice.subtract(BigDecimal.valueOf(exchangeProperties.getPriceDifference())));
                            } else {
                                return resultPriceDouble;
                            }
                        }
                        return resultPriceSimple;
                    } else if (item.getCancelType() == ExchangeCancelType.FROM_SERVICE) {
                        return minExchangePrice;
                    }
                    BigDecimalWrapper newPrice = processingPrice(lastOrder.get().getPrice(), BigDecimal.valueOf(exchangeProperties.getDoublePriceDifference()), true);
                    if (newPrice.isGreaterThen(minExchangePrice)) {
                        return minExchangePrice;
                    }
                    return newPrice;
                })
                .orElse(minExchangePrice);
    }

    private BigDecimalWrapper processingPrice(BigDecimalWrapper price, BigDecimal changingSize, boolean isAdding) {
        if (isAdding) {
            return new BigDecimalWrapper(price.add(changingSize).toString());
        } else {
            return new BigDecimalWrapper(price.subtract(changingSize).toString());
        }
    }

    public BigDecimalWrapper getMinPriceToSell(BigDecimalWrapper buyPrice, BigDecimalWrapper buyQuantity) {
        return new BigDecimalWrapper(((buyPrice.doubleValue() * buyQuantity.doubleValue()) + (buyPrice.doubleValue() * buyQuantity.doubleValue() * 0.001) + 0.01) / (buyQuantity.doubleValue() - 0.001 * buyQuantity.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimalWrapper getPriceToSell(BigDecimal currentPrice, ExchangeHistoryDto lastBuyExchange, Optional<ExchangeHistoryEntity> lastOrder) {

        BigDecimalWrapper minExchangePrice = getMinPriceToSell(lastBuyExchange.getPrice(), lastBuyExchange.getFinalAmount());

        return lastOrder
                .filter(item -> item.getOperationType() == OrderSide.SELL)
                .map(item -> {
                    BigDecimalWrapper lastOrderPrice = new BigDecimalWrapper(item.getPrice());
                    BigDecimalWrapper resultPriceAdd = new BigDecimalWrapper(lastOrderPrice.add(BigDecimal.valueOf(exchangeProperties.getPriceDifference())).toString());
                    BigDecimalWrapper resultPriceSubtract = new BigDecimalWrapper(lastOrderPrice.subtract(BigDecimal.valueOf(exchangeProperties.getPriceDifference())).toString());
                    log.info("Current: {}\nLast: {}\nAdded: {}\nSubtracted: {}", currentPrice, lastOrderPrice, resultPriceAdd, resultPriceSubtract);
                    if (new BigDecimalWrapper(currentPrice).isLessThen(lastOrderPrice)
                            && new BigDecimalWrapper(currentPrice).isGreaterThen(resultPriceSubtract)) {
                        log.info("Branch 1\n");
                        return resultPriceAdd.setScale(2, RoundingMode.HALF_UP);
                    } else if (new BigDecimalWrapper(currentPrice).isLessThen(lastOrderPrice)
                            && new BigDecimalWrapper(currentPrice).isLessThen(resultPriceSubtract)
                            && resultPriceSubtract.isGreaterThen(minExchangePrice)
                            && item.getCancelType() == ExchangeCancelType.BY_LACK_OF_EXCHANGE) {
                        log.info("Branch 2\n");
                        return resultPriceSubtract.setScale(2, RoundingMode.HALF_UP);
                    }
                    log.info("Branch 3\n");
                    return new BigDecimalWrapper(item.getPrice()).setScale(2, RoundingMode.HALF_UP);
                })
                .orElse(minExchangePrice);
    }


    private BigDecimalWrapper getMaxPriceByQuantity(List<String[]> list, OrderSide orderSide) {
        double price = getCurrentPrice().doubleValue();
        Map<Double, List<String[]>> mapPrice = list.stream()
                .collect(groupingBy(item -> Double.valueOf(item[PRICE_INDEX].substring(0, item[PRICE_INDEX].indexOf('.') + 2))));

        Optional<Map.Entry<Double, List<String[]>>> max = mapPrice.entrySet().stream()
                .filter(entry -> Math.abs(entry.getKey() - price) < exchangeProperties.getPriceDifference())
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
