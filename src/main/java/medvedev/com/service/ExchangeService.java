package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.component.TimestampComponent;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.BalanceInfoResponse;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.*;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private static final int TIME_LONG_TO_EXCHANGE = 5;
    private static final int TIME_BETWEEN_EXCHANGES = 10;
    private final ExchangeHistoryService exchangeHistoryService;
    private final PriceProcessingService priceProcessingService;
    private final BinanceApiClient binanceApiClient;
    private final BinanceProperty binanceProperty;
    private final TimestampComponent timestampComponent;

    @Scheduled(cron = "${exchange.cron.crete-buy-order}")
    public void createBuyOrder() {

        if (isNotAvailableCreateBuyOrder()) {
            return;
        }

        BigDecimalWrapper priceToBuy = priceProcessingService.getPriceToBuy();
        String quantity = getQuantityToBuy(priceToBuy);
        System.out.println("I am making a purchase");
        System.out.println("price: " + priceToBuy);
        System.out.println("currentPrice: " + binanceApiClient.getCurrentPrice(binanceProperty.getSymbol()).getPrice());
        System.out.println("quantity: " + quantity);
        OrderInfoResponse createOrderResponse = binanceApiClient.newOrder(binanceProperty.getSymbol(),
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.GTC,
                quantity,
                priceToBuy.toString(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
        exchangeHistoryService.saveIfNotExist(createOrderResponse);
    }

    @Scheduled(cron = "${exchange.cron.crete-sell-order}")
    public void createSellOrder() {

        if (isNotAvailableCreateSellOrder()) {
            return;
        }

        ExchangeHistoryDto lastBuyExchange = exchangeHistoryService.findLastBuyFilledExchange();
        //BigDecimalWrapper priceToSell = priceProcessingService.getPriceToSell();

        double minPriceToExchange = ((lastBuyExchange.getPrice().doubleValue() * lastBuyExchange.getFinalAmount().doubleValue()) + (lastBuyExchange.getFinalAmount().doubleValue() * lastBuyExchange.getPrice().doubleValue() * 0.001) + 0.002) / (lastBuyExchange.getFinalAmount().doubleValue() - 0.001 * lastBuyExchange.getFinalAmount().doubleValue());
        /*BigDecimalWrapper price = priceToSell.doubleValue() > lastBuyExchange.getPrice().doubleValue() + minPriceToExchange
                ? priceToSell
                : new BigDecimalWrapper(String.valueOf(((double) ((int) (minPriceToExchange * 100))) / 100));*/
        String quantity = getQuantityToSell();
        System.out.println("I am making a sale");
        System.out.println("price: " + new BigDecimalWrapper(String.valueOf(((double) ((int) (minPriceToExchange * 100))) / 100)));
        System.out.println("currentPrice: " + binanceApiClient.getCurrentPrice(binanceProperty.getSymbol()).getPrice());
        System.out.println("quantity: " + quantity);
        OrderInfoResponse createOrderResponse = binanceApiClient.newOrder(binanceProperty.getSymbol(),
                OrderSide.SELL,
                OrderType.LIMIT,
                TimeInForce.GTC,
                quantity,
                new BigDecimalWrapper(String.valueOf(((double) ((int) (minPriceToExchange * 100))) / 100)).toString(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
        exchangeHistoryService.saveIfNotExist(createOrderResponse);
    }

    @Scheduled(cron = "${exchange.cron.check-status-order}")
    public void checkStatusOrder() {
        exchangeHistoryService.findLastOrder()
                .ifPresent(item -> {
                    if (item.getOperationType() == OrderSide.BUY
                            && item.getOrderStatus() == OrderStatus.NEW
                            && item.getCreateDate().isBefore(LocalDateTime.now().minusMinutes(TIME_LONG_TO_EXCHANGE))) {
                        log.info("Cancel order {}", item.getOrderId());
                        binanceApiClient.cancelOrder(binanceProperty.getSymbol(),
                                item.getOrderId(),
                                timestampComponent.getTimestamp(),
                                binanceProperty.getRectWindow());
                    }
                    if (item.getOrderStatus() != OrderStatus.CANCELED
                            && item.getOrderStatus() != OrderStatus.FILLED) {
                        OrderInfoResponse orderInfoResponse = binanceApiClient.getOrderInfo(binanceProperty.getSymbol(),
                                item.getOrderId(),
                                timestampComponent.getTimestamp(),
                                binanceProperty.getRectWindow());
                        item.setOrderStatus(orderInfoResponse.getStatus());
                        item.setFinalAmount(orderInfoResponse.getExecutedQty());
                        exchangeHistoryService.save(item);
                    }
                });
    }

    private String getQuantityToBuy(BigDecimalWrapper priceToBuy) {
        BalanceInfoResponse balance = binanceApiClient.getBalanceInfo(Currency.USDT.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);
        return new BigDecimal(balance.getFree())
                .multiply(new BigDecimal("10000"))
                .divide(priceToBuy.multiply(new BigDecimal("10000")))
                .multiply(new BigDecimal("10000"))
                .multiply(new BigDecimal("0.999"))
                .divide(new BigDecimal("10000"))
                .round(new MathContext(4)).toString();
    }

    private String getQuantityToSell() {
        BalanceInfoResponse balance = binanceApiClient.getBalanceInfo(Currency.ETH.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);
        return new BigDecimal(balance.getFree())
                .multiply(new BigDecimal(10000))
                .multiply(new BigDecimal("0.999"))
                .divide(new BigDecimal("10000"))
                .round(new MathContext(4)).toString();
    }

    private boolean isNotAvailableCreateBuyOrder() {
        Optional<ExchangeHistoryEntity> lastExchangeOptional = exchangeHistoryService.findLastOrder();
        if (lastExchangeOptional.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastExchangeOptional.get();
            return (lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() != OrderStatus.CANCELED)
                    || (lastExchange.getOperationType() == OrderSide.SELL && lastExchange.getOrderStatus() != OrderStatus.FILLED)
                    || (lastExchange.getOperationType() == OrderSide.SELL && lastExchange.getUpdateDate().isAfter(LocalDateTime.now().minusMinutes(TIME_BETWEEN_EXCHANGES)));
        }
        return false;
    }

    private boolean isNotAvailableCreateSellOrder() {
        Optional<ExchangeHistoryEntity> lastExchangeOptional = exchangeHistoryService.findLastOrder();
        if (lastExchangeOptional.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastExchangeOptional.get();
            return ((lastExchange.getOperationType() == OrderSide.SELL && lastExchange.getOrderStatus() != OrderStatus.CANCELED
                    || lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() != OrderStatus.FILLED));
        }
        return true;
    }
}
