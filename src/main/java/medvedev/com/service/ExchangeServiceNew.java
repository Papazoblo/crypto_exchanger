package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.component.ExchangeProperties;
import medvedev.com.component.TimestampComponent;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.BalanceInfoResponse;
import medvedev.com.dto.response.OrderBookResponse;
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
public class ExchangeServiceNew {

    protected static final String EXCHANGE_MESSAGE_PATTERN = "*%s*\n_price_: %s\n" +
            "_from_: %s\n_to_: %s";
    public static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
    private final ExchangeProperties exchangeProperties;
    private final ExchangeHistoryService exchangeHistoryService;
    private final PriceProcessingService priceProcessingService;
    private final BinanceApiClient binanceApiClient;
    private final BinanceProperty binanceProperty;
    private final TimestampComponent timestampComponent;
    private final AssetBalanceService assetBalanceService;
    private final TelegramPollingService telegramPollingService;
    private final PriceHistoryService priceHistoryService;


    @Scheduled(cron = "${exchange.cron.every-3-sec}")
    public void createBuyOrder() {

        Optional<ExchangeHistoryEntity> lastOrder = exchangeHistoryService.findLastOrder();
        BigDecimalWrapper currentPrice = priceProcessingService.getCurrentPrice();
        if (isAvailableCreateBuyOrder(lastOrder, currentPrice)) {
            if (priceHistoryService.isPriceDifferenceLong()) {
                log.info("Price difference is LONG: {}", currentPrice);
                return;
            }
            BigDecimalWrapper priceToBuy = priceProcessingService.getPriceToBuy();
            currentPrice = priceProcessingService.getCurrentPrice();
            if (currentPrice.isLessThen(priceToBuy)) {
                priceToBuy = currentPrice;
            }

            String quantity = getQuantityToBuy(priceToBuy);
            log.info("I am making a purchase\nprice: {}\ncurrentPrice: {}\nquantity: {}\n", priceToBuy, currentPrice, quantity);

            if (lastOrder.isPresent() && lastOrder.get().getOperationType() == BUY) {
                ExchangeHistoryEntity lastExchange = lastOrder.get();
                lastExchange.setOrderStatus(NEW);
                OrderInfoResponse createOrderResponse = binanceApiClient.newOrder(binanceProperty.getSymbol(),
                        BUY,
                        OrderType.LIMIT,
                        TimeInForce.GTC,
                        quantity,
                        priceToBuy.toString(),
                        timestampComponent.getTimestamp(),
                        binanceProperty.getRectWindow());
                lastExchange.setOrderId(createOrderResponse.getOrderId());
                exchangeHistoryService.save(lastExchange);
            } else {
                createOrder(null, quantity,
                        priceToBuy.toString(),
                        OrderSide.BUY);
            }
        }
    }

    @Scheduled(cron = "${exchange.cron.every-6-sec}")
    public void createSellOrder() {

        Optional<ExchangeHistoryEntity> lastOrder = exchangeHistoryService.findLastOrder();
        BigDecimalWrapper currentPrice = priceProcessingService.getCurrentPrice();
        if (isAvailableCreateSellOrder(lastOrder)) {
            createSellOrder(lastOrder.orElse(null), currentPrice);
        }
    }

    /**
     * если покупка, то отмена по курсу
     * если продажа, то отмена по времени
     */
    @Scheduled(cron = "${exchange.cron.every-3-sec}")
    public void cancelOrderToPriceCorrecting() {
        BigDecimal currentPrice = priceProcessingService.getCurrentPrice();
        exchangeHistoryService.findLastOrder().ifPresent(item -> {
            if (item.getOrderStatus() == NEW) {
                if (item.getOperationType() == BUY) {
                    if (item.getUpdateDate().isBefore(LocalDateTime.now().minusSeconds(exchangeProperties.getTimeLongToBackChangePrice()))) {
                        cancelOrder(currentPrice, item, ExchangeCancelType.BY_LACK_OF_EXCHANGE);
                    } else if (item.getOperationType() == BUY && item.getUpdateDate().isBefore(LocalDateTime.now().minusSeconds(exchangeProperties.getTimeLiveBuyExchange()))) {
                        if (new BigDecimalWrapper(item.getPrice()).isGreaterThen(currentPrice.subtract(BigDecimal.valueOf(exchangeProperties.getDoublePriceDifference())))) {
                            cancelOrder(currentPrice, item, ExchangeCancelType.BY_PRICE_ADJUSTMENT);
                        }
                    }
                } else {
                    log.info("Is {} > {}", LocalDateTime.now().minusMinutes(exchangeProperties.getMinutesCountLiveSellOrder()), item.getUpdateDate());
                    if (LocalDateTime.now().minusMinutes(exchangeProperties.getMinutesCountLiveSellOrder()).isAfter(item.getUpdateDate())) {
                        cancelOrder(item, ExchangeCancelType.BY_PRICE_ADJUSTMENT);
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
                item.setFinalAmount(new BigDecimalWrapper(orderInfoResponse.getExecutedQty()).toString());
                item.setPrice(orderInfoResponse.getPrice());
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
                                item.getInitialAmount().multiply(item.getPrice()).setScale(4, RoundingMode.HALF_EVEN)));
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
            if (newPrice.compareTo(exchange.getPrice().setScale(2, RoundingMode.HALF_UP)) == 0) {
                return;
            }
            cancelOrder(exchange, cancelType);
        } catch (Exception ex) {
            log.info("Error cancelling order: {}", ex.getMessage());
            checkStatusOrder();
        }
    }

    private void cancelOrder(ExchangeHistoryEntity exchange, ExchangeCancelType cancelType) {
        log.info("Canceling order [id = {}, orderId = {}]", exchange.getId(), exchange.getOrderId());
        binanceApiClient.cancelOrder(binanceProperty.getSymbol(),
                exchange.getOrderId(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
        exchange.setCancelType(cancelType);
        exchangeHistoryService.save(exchange);
    }

    private void createSellOrder(ExchangeHistoryEntity lastExchange, BigDecimalWrapper currentPrice) {
        if (lastExchange == null) {
            return;
        }

        if (lastExchange.getOperationType() == BUY) {

            String quantity = new BigDecimal(binanceApiClient.getBalanceInfo(Currency.ETH.name(),
                    timestampComponent.getTimestamp(),
                    binanceProperty.getRectWindow()).get(0).getFree()).multiply(BigDecimal.valueOf(0.9998)).setScale(4, RoundingMode.DOWN).toString();

            BigDecimalWrapper priceToSell = priceProcessingService.getMinPriceToSell(lastExchange.getPrice(), lastExchange.getInitialAmount());
            if (priceToSell.isLessThenOrEqual(currentPrice)) {
                priceToSell = currentPrice;
            }
            log.info("I am making a sale\nprice: {}\ncurrentPrice: {}\nquantity: {}\n", priceToSell, currentPrice, quantity);
            createOrder(lastExchange, quantity,
                    priceToSell.toString(),
                    SELL);
        } else {
            /**
             -если step = 50, то сбарсываем на 0
             -если курс < минимального обмена, то минимальный для обмена
             -если курс > минимального, то текущий курс
             -если step < 50, то инекрементим
             */
            lastExchange.setOrderStatus(NEW);
            if (lastExchange.getIncrementStep() < 75) {
                lastExchange.setIncrementStep(lastExchange.getIncrementStep() + 1);
                if (currentPrice.isLessThen(lastExchange.getMinPriceExchange())) {
                    lastExchange.setPrice(new BigDecimalWrapper(lastExchange.getMinPriceExchange().add(BigDecimal.valueOf(exchangeProperties.getSellPriceIncrement()).multiply(new BigDecimal(lastExchange.getIncrementStep())))).setScale(2, RoundingMode.HALF_DOWN).toString());
                } else {
                    lastExchange.setPrice(new BigDecimalWrapper(currentPrice.add(BigDecimal.valueOf(exchangeProperties.getSellPriceIncrement()).multiply(new BigDecimal(lastExchange.getIncrementStep())))).setScale(2, RoundingMode.HALF_DOWN).toString());
                }
            } else {
                lastExchange.setIncrementStep(1);
                if (currentPrice.isLessThen(lastExchange.getMinPriceExchange())) {
                    lastExchange.setPrice(new BigDecimalWrapper(lastExchange.getMinPriceExchange().add(BigDecimal.valueOf(exchangeProperties.getSellPriceIncrement()).multiply(new BigDecimal(lastExchange.getIncrementStep())))).setScale(2, RoundingMode.HALF_DOWN).toString());
                } else {
                    lastExchange.setPrice(new BigDecimalWrapper(currentPrice.add(BigDecimal.valueOf(exchangeProperties.getSellPriceIncrement()).multiply(new BigDecimal(lastExchange.getIncrementStep())))).setScale(2, RoundingMode.HALF_DOWN).toString());
                }
            }
            OrderInfoResponse createOrderResponse = binanceApiClient.newOrder(binanceProperty.getSymbol(),
                    SELL,
                    OrderType.LIMIT,
                    TimeInForce.GTC,
                    lastExchange.getInitialAmount().toString(),
                    lastExchange.getPrice().toString(),
                    timestampComponent.getTimestamp(),
                    binanceProperty.getRectWindow());
            lastExchange.setOrderId(createOrderResponse.getOrderId());
            exchangeHistoryService.save(lastExchange);

            log.info("I am making a sale\nprice: {}\ncurrentPrice: {}\nquantity: {}\n", createOrderResponse.getPrice(), currentPrice, lastExchange.getInitialAmount());
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
                .divide(TEN_THOUSAND, 4, RoundingMode.DOWN)
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

    private boolean isAvailableCreateBuyOrder(Optional<ExchangeHistoryEntity> lastOrder, BigDecimalWrapper curPrice) {

        OrderBookResponse response = binanceApiClient.getOrderBook(binanceProperty.getSymbol(), 150);
        double sumBuy = response.getBids().stream()
                .mapToDouble(item -> Double.parseDouble(item[OrderBookResponse.QUANTITY_INDEX]))
                .sum();
        double sumSell = response.getAsks().stream()
                .mapToDouble(item -> Double.parseDouble(item[OrderBookResponse.QUANTITY_INDEX]))
                .sum();
        log.info("Price: {}\n Buy: {}\n Sell: {}\n Diff: {}\n Coeff: {}\n\n", curPrice, sumBuy, sumSell, sumBuy - sumSell, sumBuy / sumSell);

        if (sumBuy / sumSell < 2.45) {
            return false;
        }

        if (lastOrder.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastOrder.get();
            return (lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() == OrderStatus.CANCELED)
                    || (lastExchange.getOperationType() == SELL
                    && lastExchange.getOrderStatus() == OrderStatus.FILLED
                    && lastExchange.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(exchangeProperties.getTimeBetweenExchange()))
            );
        }
        return true;
    }

    private boolean isAvailableCreateSellOrder(Optional<ExchangeHistoryEntity> lastOrder) {
        if (lastOrder.isPresent()) {
            ExchangeHistoryEntity lastExchange = lastOrder.get();
            return ((lastExchange.getOperationType() == SELL
                    && lastExchange.getOrderStatus() == OrderStatus.CANCELED)
                    || (lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() == OrderStatus.FILLED))
                    && lastExchange.getUpdateDate().isBefore(LocalDateTime.now().minusSeconds(90));
        }
        return false;
    }
}
