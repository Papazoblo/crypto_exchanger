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
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static medvedev.com.enums.OrderSide.BUY;
import static medvedev.com.enums.OrderSide.SELL;
import static medvedev.com.enums.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    protected static final String EXCHANGE_MESSAGE_PATTERN = "*%s*\n_price_: %s\n" +
            "_from_: %s\n_to_: %s";
    public static final BigDecimal PRICE_DIFFERENCE = new BigDecimal("0.75");
    public static final BigDecimal DOUBLE_PRICE_DIFFERENCE = new BigDecimal("1.0");
    public static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
    private static final int TIME_LONG_TO_BUY_EXCHANGE_SEC = 16;
    private static final int TIME_LONG_TO_SELL_EXCHANGE_SEC = 16;
    private static final int TIME_LONG_TO_BACK_CHANGE_PRICE = 32;
    private static final int TIME_BETWEEN_EXCHANGES = 45;
    private final ExchangeHistoryService exchangeHistoryService;
    private final PriceProcessingService priceProcessingService;
    private final BinanceApiClient binanceApiClient;
    private final BinanceProperty binanceProperty;
    private final TimestampComponent timestampComponent;
    private final AssetBalanceService assetBalanceService;
    private final TelegramPollingService telegramPollingService;
    private final PriceHistoryService priceHistoryService;


    @Scheduled(cron = "${exchange.cron.every-3-sec}")
    public void createOrder() {

        Optional<ExchangeHistoryEntity> lastOrder = exchangeHistoryService.findLastOrder();
        if (isAvailableCreateBuyOrder(lastOrder)) {
            if (priceHistoryService.isPriceDifferenceLong()) {
                log.info("Price difference is LONG");
                return;
            }
            BigDecimalWrapper priceToBuy = priceProcessingService.getPriceToBuy(lastOrder);
            String quantity = getQuantityToBuy(priceToBuy);
            System.out.println("I am making a purchase");
            System.out.println("price: " + priceToBuy);
            System.out.println("currentPrice: " + binanceApiClient.getCurrentPrice(binanceProperty.getSymbol()).getPrice());
            System.out.println("quantity: " + quantity);
            createOrder(lastOrder.orElse(null), quantity,
                    priceToBuy.toString(),
                    OrderSide.BUY);
        } else if (isAvailableCreateSellOrder(lastOrder)) {
            ExchangeHistoryDto lastBuyExchange = exchangeHistoryService.findLastBuyFilledExchange();
            BigDecimalWrapper priceToSell = priceProcessingService.getPriceToSell(priceProcessingService.getCurrentPrice(), lastBuyExchange, lastOrder);
            String quantity = getQuantityToSell();
            System.out.println("I am making a sale");
            System.out.println("price: " + priceToSell);
            System.out.println("currentPrice: " + binanceApiClient.getCurrentPrice(binanceProperty.getSymbol()).getPrice());
            System.out.println("quantity: " + quantity);
            createOrder(lastOrder.orElse(null), quantity,
                    priceToSell.toString(),
                    SELL);
        }
    }

    @Scheduled(cron = "${exchange.cron.every-2-sec}")
    public void cancelOrderToPriceCorrecting() {
        BigDecimal currentPrice = priceProcessingService.getCurrentPrice();
        exchangeHistoryService.findLastOrder().ifPresent(item -> {
            if (item.getOrderStatus() == NEW) {
                if (item.getCreateDate().isBefore(LocalDateTime.now().minusSeconds(TIME_LONG_TO_BACK_CHANGE_PRICE))) {
                    cancelOrder(currentPrice, item, ExchangeCancelType.BY_LACK_OF_EXCHANGE);
                } else if (item.getOperationType() == SELL && item.getCreateDate().isBefore(LocalDateTime.now().minusSeconds(TIME_LONG_TO_SELL_EXCHANGE_SEC))) {
                    log.info("{} {} > {}", item.getOperationType(), new BigDecimalWrapper(item.getPrice()), currentPrice.add(DOUBLE_PRICE_DIFFERENCE));
                    if (new BigDecimalWrapper(item.getPrice()).isLessThen(currentPrice.add(DOUBLE_PRICE_DIFFERENCE))) {
                        cancelOrder(currentPrice, item, ExchangeCancelType.BY_PRICE_ADJUSTMENT);
                    }
                } else if (item.getOperationType() == BUY && item.getCreateDate().isBefore(LocalDateTime.now().minusSeconds(TIME_LONG_TO_BUY_EXCHANGE_SEC))) {
                    log.info("{} {} < {}", item.getOperationType(), new BigDecimalWrapper(item.getPrice()), currentPrice.subtract(PRICE_DIFFERENCE));
                    if (new BigDecimalWrapper(item.getPrice()).isGreaterThen(currentPrice.subtract(PRICE_DIFFERENCE))) {
                        cancelOrder(currentPrice, item, ExchangeCancelType.BY_PRICE_ADJUSTMENT);
                    }
                }
            }
        });
        priceHistoryService.savePrice(new BigDecimalWrapper(currentPrice));
    }

    private BigDecimalWrapper getNewPrice(BigDecimal currentPrice, ExchangeHistoryEntity item) {
        BigDecimalWrapper newPrice;
        if (item.getOperationType() == SELL) {
            ExchangeHistoryDto lastBuyExchange = exchangeHistoryService.findLastBuyFilledExchange();
            newPrice = priceProcessingService.getPriceToSell(currentPrice, lastBuyExchange, Optional.of(item));
        } else {
            newPrice = priceProcessingService.getPriceToBuy(Optional.of(item)).setScale(2, RoundingMode.HALF_UP);
        }
        return newPrice;
    }

    @Scheduled(cron = "${exchange.cron.every-6-sec}")
    public void checkStatusOrder() {
        exchangeHistoryService.findLastOrder().ifPresent(item -> {
            if (item.getOrderStatus() != OrderStatus.CANCELED && item.getOrderStatus() != OrderStatus.FILLED) {
                OrderInfoResponse orderInfoResponse = binanceApiClient.getOrderInfo(binanceProperty.getSymbol(),
                        item.getOrderId(),
                        timestampComponent.getTimestamp(),
                        binanceProperty.getRectWindow());
                item.setOrderStatus(orderInfoResponse.getStatus());
                item.setFinalAmount(orderInfoResponse.getExecutedQty());
                if (item.getOrderStatus() == CANCELED && item.getCancelType() == null) {
                    item.setCancelType(ExchangeCancelType.FROM_SERVICE);
                }
                log.debug("Check status launched: {}", orderInfoResponse);
                exchangeHistoryService.save(item);

                if (item.getOrderStatus() == FILLED) {
                    try {
                        telegramPollingService.sendMessage(String.format(EXCHANGE_MESSAGE_PATTERN, item.getOperationType().name(),
                                item.getPrice(),
                                item.getInitialAmount(),
                                new BigDecimal(item.getInitialAmount()).multiply(new BigDecimal(item.getPrice())).setScale(4, RoundingMode.HALF_EVEN)));
                    } catch (Exception ex) {
                        log.debug("Error send TG message: {}", ex.getMessage());
                    }

                    if (item.getOperationType() == SELL) {
                        assetBalanceService.create();
                    }
                }
            }
        });
    }

    private void cancelOrder(BigDecimal currentPrice, ExchangeHistoryEntity exchange, ExchangeCancelType cancelType) {
        try {
            exchange.setCancelType(cancelType);
            BigDecimalWrapper newPrice = getNewPrice(currentPrice, exchange);
            log.info("[{}] eq? {} = {}, {}", currentPrice.toString(),
                    newPrice.toString(),
                    new BigDecimalWrapper(exchange.getPrice()).setScale(2, RoundingMode.HALF_UP),
                    cancelType.name());
            if (newPrice.toString().equals(new BigDecimalWrapper(exchange.getPrice()).setScale(2, RoundingMode.HALF_UP).toString())) {
                return;
            }
            log.info("Cancel order [id = {}, orderId = {}]", exchange.getId(), exchange.getOrderId());
            binanceApiClient.cancelOrder(binanceProperty.getSymbol(),
                    exchange.getOrderId(),
                    timestampComponent.getTimestamp(),
                    binanceProperty.getRectWindow());
            exchange.setCancelType(cancelType);
            exchangeHistoryService.save(exchange);
        } catch (Exception ex) {
            log.info("Error cancelling order: {}", ex.getMessage());
            checkStatusOrder();
        }
    }

    private void createOrder(ExchangeHistoryEntity lastExchange, String quantity, String price, OrderSide orderSide) {
        OrderInfoResponse createOrderResponse = binanceApiClient.newOrder(binanceProperty.getSymbol(),
                orderSide,
                OrderType.LIMIT,
                TimeInForce.GTC,
                quantity,
                price,
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
        exchangeHistoryService.saveIfNotExist(lastExchange, createOrderResponse);
    }

    private String getQuantityToBuy(BigDecimalWrapper priceToBuy) {
        BalanceInfoResponse balance = binanceApiClient.getBalanceInfo(Currency.USDT.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);

        return new BigDecimal(balance.getFree())
                .multiply(TEN_THOUSAND)
                .divide(priceToBuy.multiply(TEN_THOUSAND), 4, RoundingMode.HALF_DOWN)
                .multiply(TEN_THOUSAND)
                .multiply(new BigDecimal("0.995"))
                .divide(TEN_THOUSAND, 4, RoundingMode.HALF_DOWN)
                .round(new MathContext(4)).toString();
    }

    private String getQuantityToSell() {
        BalanceInfoResponse balance = binanceApiClient.getBalanceInfo(Currency.ETH.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);
        return new BigDecimal(balance.getFree())
                .multiply(TEN_THOUSAND)
                .multiply(new BigDecimal("0.999"))
                .divide(TEN_THOUSAND, 4, RoundingMode.FLOOR)
                .round(new MathContext(4)).toString();
    }

    private boolean isAvailableCreateBuyOrder(Optional<ExchangeHistoryEntity> lastOrder) {
        if (lastOrder.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastOrder.get();
            return (lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() == OrderStatus.CANCELED)
                    || (lastExchange.getOperationType() == SELL
                    && lastExchange.getOrderStatus() == OrderStatus.FILLED
                    && lastExchange.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(TIME_BETWEEN_EXCHANGES)));
        }
        return true;
    }

    private boolean isAvailableCreateSellOrder(Optional<ExchangeHistoryEntity> lastOrder) {
        if (lastOrder.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastOrder.get();
            return ((lastExchange.getOperationType() == SELL && lastExchange.getOrderStatus() == OrderStatus.CANCELED
                    || lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() == OrderStatus.FILLED));
        }
        return false;
    }
}
