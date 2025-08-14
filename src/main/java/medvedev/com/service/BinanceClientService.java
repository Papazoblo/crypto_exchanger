package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.component.TimestampComponent;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.BalanceInfoResponse;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.enums.Currency;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderType;
import medvedev.com.enums.TimeInForce;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BinanceClientService {

    private final BinanceApiClient client;
    private final TimestampComponent timestampComponent;
    private final BinanceProperty binanceProperty;

    public BalanceInfoResponse getBalance(Currency currency) {
        return client.getBalanceInfo(currency.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);
    }

    public BigDecimalWrapper getCurrentPrice() {
        return BigDecimalWrapper.of(client.getCurrentPrice(binanceProperty.getSymbol()).getPrice());
    }

    public OrderInfoResponse getOrderInfo(Long orderId) {
        return client.getOrderInfo(binanceProperty.getSymbol(),
                orderId,
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
    }

    public void cancelOrder(Long orderId) {
        client.cancelOrder(binanceProperty.getSymbol(),
                orderId,
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
    }

    public OrderInfoResponse createOrder(OrderSide orderSide,
                                         OrderType orderType,
                                         TimeInForce timeInForce,
                                         String quantity,
                                         String price) {
        return client.newOrder(binanceProperty.getSymbol(),
                orderSide,
                orderType,
                timeInForce,
                quantity,
                price,
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow());
    }
}
