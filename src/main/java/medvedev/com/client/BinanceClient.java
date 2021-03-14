package medvedev.com.client;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.market.TickerStatistics;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.enums.Currency;
import org.springframework.stereotype.Service;

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

    /**
     * Создать ордер на покупку
     *
     * @param quantity - количество
     * @return ответ по созданию ордера
     */
    public NewOrderResponse creteBuyOrder(String quantity) {
        return createOrder(NewOrder.marketBuy(property.getSymbol(), quantity));
    }

    /**
     * Создать ордер на продажу
     *
     * @param quantity
     * @return ответ по созданию ордера
     */
    public NewOrderResponse createSellOrder(String quantity) {
        return createOrder(NewOrder.marketSell(property.getSymbol(), quantity));
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

    /**
     * Получить балансе по указанной валюте
     *
     * @param currency - валюта
     * @return - объект баланса валюты
     */
    public AssetBalance getBalanceByCurrency(Currency currency) {
        return getAccountInfo().getAssetBalance(currency.name());
    }

    /**
     * Получить время сервера
     *
     * @return - время сервера
     */
    private Long getServerTime() {
        return client.getServerTime();
    }

    /**
     * Создать новый ордер
     *
     * @param order - ордер для создания
     * @return ответ по созданию ордера
     */
    private NewOrderResponse createOrder(NewOrder order) {
        order.timestamp(getServerTime());
        order.recvWindow(property.getRectWindow());
        return client.newOrder(order);
    }
}
