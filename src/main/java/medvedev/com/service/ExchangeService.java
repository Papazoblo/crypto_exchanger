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
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private static final int TIME_LONG_TO_EXCHANGE = 5;
    private final ExchangeHistoryService exchangeHistoryService;
    private final PriceProcessingService priceProcessingService;
    private final BinanceApiClient binanceApiClient;
    private final BinanceProperty binanceProperty;
    private final TimestampComponent timestampComponent;

    @Scheduled(cron = "${exchange.cron.crete-buy-order}")
    public void createBuyOrder() {

        if (isNotAvailableCreateBuyOrder()) {
            log.debug("Unable create BUY order");
            return;
        }

        BigDecimalWrapper priceToBuy = priceProcessingService.getPriceToBuy();
        OrderInfoResponse createOrderResponse = binanceApiClient.newOrder(binanceProperty.getSymbol(),
                OrderSide.BUY,
                OrderType.LIMIT,
                TimeInForce.GTC,
                getQuantityToBuy(priceToBuy),
                priceToBuy.toString(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
        exchangeHistoryService.saveIfNotExist(createOrderResponse);
    }

    @Scheduled(cron = "${exchange.cron.check-status-order}")
    public void checkStatusBuyOrder() {
        exchangeHistoryService.findLastOrder()
                .ifPresent(item -> {
                    if (item.getOrderStatus() == OrderStatus.NEW && item.getDateTime().isBefore(LocalDateTime.now().minusMinutes(TIME_LONG_TO_EXCHANGE))) {
                        log.info("Cancel order {}", item.getOrderId());
                        binanceApiClient.cancelOrder(binanceProperty.getSymbol(),
                                item.getOrderId(),
                                timestampComponent.getTimestamp(),
                                binanceProperty.getRectWindow());
                    } else if (item.getOrderStatus() != OrderStatus.CANCELED
                            && item.getOrderStatus() != OrderStatus.FILLED) {
                        log.info("Check order status {}", item.getOrderId());
                        OrderInfoResponse orderInfoResponse = binanceApiClient.getOrderInfo(binanceProperty.getSymbol(),
                                item.getOrderId(),
                                timestampComponent.getTimestamp(),
                                binanceProperty.getRectWindow());
                        item.setOrderStatus(orderInfoResponse.getStatus());
                        exchangeHistoryService.save(item);
                    }
                });
    }

    @Scheduled(cron = "${exchange.cron.crete-sell-order}")
    public void createSellOrder() {

        if (isNotAvailableCreateSellOrder()) {
            log.info("Unable create SELL order");
            return;
        }

        ExchangeHistoryDto lastBuyExchange = exchangeHistoryService.findLastBuyFilledExchange();
        BigDecimalWrapper priceToSell = priceProcessingService.getPriceToSell();
        BigDecimalWrapper price = priceToSell.doubleValue() > lastBuyExchange.getPrice().doubleValue() + 0.7
                ? priceToSell
                : new BigDecimalWrapper(lastBuyExchange.getPrice().add(new BigDecimal("0.7")).toString());
        System.out.println("I want to sell");
        System.out.println("price: " + price);
        System.out.println("quantity: " + getQuantityToSell());
        OrderInfoResponse createOrderResponse = binanceApiClient.newOrder(binanceProperty.getSymbol(),
                OrderSide.SELL,
                OrderType.LIMIT,
                TimeInForce.GTC,
                getQuantityToSell(),
                price.toString(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
        exchangeHistoryService.saveIfNotExist(createOrderResponse);
    }

    private String getQuantityToBuy(BigDecimalWrapper priceToBuy) {
        BalanceInfoResponse balance = binanceApiClient.getBalanceInfo(Currency.USDT.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);
        return String.valueOf((double) ((int) ((Double.parseDouble(balance.getFree()) * 10000)
                / (priceToBuy.doubleValue() * 10000)
                * 0.999 * 10000)) / 10000);
    }

    private String getQuantityToSell() {
        BalanceInfoResponse balance = binanceApiClient.getBalanceInfo(Currency.ETH.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);
        return String.valueOf((double) ((int) ((Double.parseDouble(balance.getFree()) * 10000) * 0.999) / 10000));
    }

    private boolean isNotAvailableCreateBuyOrder() {
        Optional<ExchangeHistoryEntity> lastExchangeOptional = exchangeHistoryService.findLastOrder();
        if (lastExchangeOptional.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastExchangeOptional.get();
            return (lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() != OrderStatus.CANCELED)
                    || (lastExchange.getOperationType() == OrderSide.SELL && lastExchange.getOrderStatus() != OrderStatus.FILLED);
        }
        return false;
    }

    private boolean isNotAvailableCreateSellOrder() {
        Optional<ExchangeHistoryEntity> lastExchangeOptional = exchangeHistoryService.findLastOrder();
        if (lastExchangeOptional.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastExchangeOptional.get();
            return (lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() != OrderStatus.FILLED)
                    || (lastExchange.getOperationType() == OrderSide.SELL && lastExchange.getOrderStatus() != OrderStatus.CANCELED);
        }
        return true;
    }
}
