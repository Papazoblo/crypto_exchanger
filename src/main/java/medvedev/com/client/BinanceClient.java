package medvedev.com.client;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.*;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.market.TickerStatistics;
import javassist.NotFoundException;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.enums.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BinanceClient {

    private final BinanceApiRestClient client;
    private final BinanceProperty property;

    public BinanceClient(BinanceProperty property) {
        this.property = property;
        client = BinanceApiClientFactory.newInstance(property.getKey(), property.getSecretKey()).newRestClient();
    }

    /**
     * Получить информацию о курсе
     *
     * @return информация о курсе
     */
    public TickerStatistics getPriceInfo() {
        return client.get24HrPriceStatistics(property.getSymbol());
    }

    public NewOrderResponse createBuyOrder(BigDecimal quantity) {
        return createNewOrder(NewOrder.marketBuy(property.getSymbol(), quantity.toString()));
    }

    public NewOrderResponse createSellOrder(BigDecimal quantity) {
        return createNewOrder(NewOrder.marketSell(property.getSymbol(), quantity.toString()));
    }

    public Order getLastFilledOrder() throws NotFoundException {
        List<Order> orders = getAllOrders();
        if (orders.isEmpty()) {
            throw new NotFoundException("Orders not found");
        } else {
            for (int i = orders.size() - 1; i >= 0; i--) {
                Order order = orders.get(i);
                if (order.getStatus() == OrderStatus.FILLED) {
                    return order;
                }
            }
            throw new NotFoundException("Orders not found");
        }
    }

    public List<Order> getAllOrders() {
        AllOrdersRequest request = new AllOrdersRequest(property.getSymbol());
        request.timestamp(client.getServerTime());
        return client.getAllOrders(request);
    }

    public OrderStatus getOrderStatus(Long orderId) {
        OrderStatusRequest request = new OrderStatusRequest(property.getSymbol(), orderId);
        Order response = client.getOrderStatus(request);
        return response.getStatus();
    }

    /**
     * Получить информацию об аккаунте
     *
     * @return - информация об аккаунте
     */
    public Account getAccountInfo() {
        return client.getAccount(
                property.getRectWindow(),
                getServerTime()
        );
    }

    public AssetBalance getBalanceByCurrency(Currency currency) {
        return getAccountInfo().getAssetBalance(currency.name());
    }

    private Long getServerTime() {
        return client.getServerTime();
    }

    private NewOrderResponse createNewOrder(NewOrder order) {
        order.timestamp(getServerTime());
        order.recvWindow(property.getRectWindow());
        return client.newOrder(order);
    }
}
