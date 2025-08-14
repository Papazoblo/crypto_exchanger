package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import medvedev.com.component.ExchangeProperties;
import medvedev.com.dto.event.CreateBuyOrderEvent;
import medvedev.com.dto.event.PriceChangeEvent;
import medvedev.com.dto.response.BalanceInfoResponse;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.Currency;
import medvedev.com.enums.ExchangeCancelType;
import medvedev.com.enums.OrderSide;
import static medvedev.com.enums.OrderSide.BUY;
import static medvedev.com.enums.OrderSide.SELL;
import medvedev.com.enums.OrderStatus;
import static medvedev.com.enums.OrderStatus.*;
import medvedev.com.enums.OrderType;
import medvedev.com.enums.TimeInForce;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    protected static final String EXCHANGE_MESSAGE_PATTERN = "*%s*\n_price_: %s\n" +
            "_from_: %s\n_to_: %s\n_price_to_sell_: %s";
    private static final int MAX_ORDER_HOUR_SIZE = 3;
    public static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
    private final ExchangeProperties exchangeProperties;
    private final ExchangeHistoryService exchangeHistoryService;
    private final PriceProcessingService priceProcessingService;
    private final BinanceClientService binanceClientService;
    private final AssetBalanceService assetBalanceService;
    private final TelegramPollingService telegramPollingService;

    @EventListener
    public void listenCreateBuyOrder(CreateBuyOrderEvent event) {
        createBuyOrder(event.getLastBlock(), event.getPriceToBuy(), event.getPriceToSell());
    }

    public void createBuyOrder(PriceHistoryBlockEntity lastBlock,
                               BigDecimalWrapper priceToBuy,
                               String priceToSell) {

        BigDecimalWrapper currentPrice = binanceClientService.getCurrentPrice();
        if (isAvailableCreateBuyOrder(Optional.empty(), currentPrice)) {

            if (currentPrice.isLessThen(priceToBuy)) {
                priceToBuy = currentPrice;
            }

            String quantity = getQuantityToBuy(priceToBuy);
            log.info("I am making a purchase\nprice: {}\ncurrentPrice: {}\nquantity: {}\n", priceToBuy, currentPrice, quantity);
            createOrder(null, quantity,
                    priceToBuy.toString(),
                    OrderSide.BUY,
                    lastBlock,
                    priceToSell);
        }
    }

    /**
     * Проверка стоп цены SELL ордера
     *
     * @param changeEvent
     */
    @EventListener
    public void checkStopPrice(PriceChangeEvent changeEvent) {
        exchangeHistoryService.findFirst(BUY, NEW).ifPresent(sellExchange -> {
            if (BigDecimalWrapper.of(sellExchange.getStopPrice()).isGreaterThen(changeEvent.getPrice())) {
                cancelOrder(sellExchange, ExchangeCancelType.BY_BROKE_STOP_PRICE); //todo
            }
        });
    }

    @Scheduled(cron = "${exchange.cron.every-20-sec}")
    public void checkStatusOrder() {
        BigDecimalWrapper currentPrice = binanceClientService.getCurrentPrice();
        exchangeHistoryService.findAllByStatus(Arrays.asList(NEW, PARTIALLY_FILLED)).forEach(item -> {
            OrderInfoResponse orderInfoResponse = binanceClientService.getOrderInfo(item.getOrderId());
            item.setOrderStatus(orderInfoResponse.getStatus());
            item.setFinalAmount(new BigDecimalWrapper(orderInfoResponse.getExecutedQty()).toString());
            item.setPrice(orderInfoResponse.getPrice());
            log.debug("Check status order: {}", orderInfoResponse);
            item = exchangeHistoryService.save(item);

            if (item.getOrderStatus() == CANCELED && item.getOperationType() == SELL) {
                if (item.getCancelType() == ExchangeCancelType.BY_BROKE_STOP_PRICE
                        || item.getCancelType() == ExchangeCancelType.BY_LACK_OF_EXCHANGE) {
                    createSellOrder(item.getPrevExchange(), currentPrice);
                }
            } else if (item.getOrderStatus() == FILLED) {

                try {
                    telegramPollingService.sendMessage(String.format(EXCHANGE_MESSAGE_PATTERN, item.getOperationType().name(),
                            item.getPrice(),
                            item.getInitialAmount(),
                            item.getInitialAmount().multiply(item.getPrice()).setScale(4, RoundingMode.HALF_EVEN),
                            item.getPriceToSell()));
                } catch (Exception ex) {
                    log.debug("Error send TG message: {}", ex.getMessage());
                }

                if (item.getOperationType() == BUY) {//если ордер на покупку исполнился
                    //createSellOrder(item, null); перенесено в крон
                } else {
                    assetBalanceService.create();
                }
            } else {
                if (item.getOperationType() == BUY) {

                } else {
//                    if (new BigDecimalWrapper(item.getStopPrice()).isGreaterThen(currentPrice)) {// если текущий курс меньше стоп прайса
//                        cancelOrder(item, ExchangeCancelType.BY_BROKE_STOP_PRICE);
//                    } else if (item.getCreateDate().isBefore(LocalDateTime.now().minusHours(MAX_ORDER_HOUR_SIZE))) {//если ордер не исполнился за ${MAX_ORDER_HOUR_SIZE} часов
//                        cancelOrder(item, ExchangeCancelType.BY_LACK_OF_EXCHANGE);
//                    }
                }
            }
        });
    }

    private void cancelOrder(ExchangeHistoryEntity exchange, ExchangeCancelType cancelType) {
        log.info("Canceling order [id = {}, orderId = {}]", exchange.getId(), exchange.getOrderId());
        binanceClientService.cancelOrder(exchange.getOrderId());
        exchange.setCancelType(cancelType);
        exchangeHistoryService.save(exchange);
    }

    @Scheduled(cron = "${exchange.cron.every-1-min}")
    public void createSellByCron() {
        exchangeHistoryService.findLast().ifPresent(item -> createSellOrder(item, null));
    }

    public void createSellOrder(ExchangeHistoryEntity buyOrder, BigDecimalWrapper sellPrice) {

        if (buyOrder == null || buyOrder.getOperationType() == SELL) {
            return;
        }

        if (isAvailableCreateSellOrder(Optional.of(buyOrder))) {

            String quantity = new BigDecimal(binanceClientService.getBalance(Currency.ETH).getFree())
                    .multiply(BigDecimal.valueOf(0.9998)).setScale(4, RoundingMode.DOWN).toString();

            BigDecimalWrapper priceToSell = Optional.ofNullable(sellPrice).orElse(BigDecimalWrapper.of(buyOrder.getPriceToSell()));
            log.info("I am making a sale\nprice: {}\nquantity: {}\n", priceToSell, quantity);
            createOrder(buyOrder, quantity,
                    priceToSell.toString(),
                    SELL,
                    null,
                    null);
        }
    }

    private void createOrder(ExchangeHistoryEntity lastExchange,
                             String quantity,
                             String price,
                             OrderSide orderSide,
                             PriceHistoryBlockEntity priceHistoryBlock,
                             String priceToSell) {

        if (new BigDecimalWrapper(quantity).isEqual(new BigDecimal(0))) {
            log.debug("Failed create order. Quantity is incorrect [{}]", quantity);
            return;
        }

        OrderInfoResponse createOrderResponse = binanceClientService.createOrder(orderSide,
                OrderType.LIMIT,
                TimeInForce.GTC,
                quantity,
                price);

        exchangeHistoryService.saveIfNotExist(lastExchange,
                createOrderResponse,
                priceHistoryBlock,
                priceToSell);
    }

    private String getQuantityToBuy(BigDecimalWrapper priceToBuy) {
        BalanceInfoResponse balance = binanceClientService.getBalance(Currency.USDT);

        return new BigDecimal(balance.getFree())
                .multiply(TEN_THOUSAND)
                .divide(priceToBuy.multiply(TEN_THOUSAND), 4, RoundingMode.HALF_DOWN)
                .multiply(TEN_THOUSAND)
                .multiply(new BigDecimal("0.995"))
                .divide(TEN_THOUSAND, 4, RoundingMode.DOWN)
                .round(new MathContext(4)).toString();
    }

    private boolean isAvailableCreateBuyOrder(Optional<ExchangeHistoryEntity> lastOrder, BigDecimalWrapper curPrice) {


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
            return ((lastExchange.getOperationType() == SELL && lastExchange.getOrderStatus() == OrderStatus.CANCELED)
                    || (lastExchange.getOperationType() == OrderSide.BUY && lastExchange.getOrderStatus() == OrderStatus.FILLED));
        }
        return false;
    }
}
