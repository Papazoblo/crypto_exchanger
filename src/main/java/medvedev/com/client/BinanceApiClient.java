package medvedev.com.client;


import medvedev.com.component.BinanceApiInterceptor;
import medvedev.com.dto.response.*;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderType;
import medvedev.com.enums.TimeInForce;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "BinanceFeignClient", url = "${binance.url}",
        configuration = {BinanceApiInterceptor.class})
public interface BinanceApiClient {

    @GetMapping(value = "/api/v3/ticker/price")
    GetCurrentPriceResponse getCurrentPrice(@RequestParam("symbol") String symbol);

    @GetMapping(value = "/api/v3/allOrders")
    List<OrderInfoResponse> getAllOrders(@RequestParam("symbol") String symbol,
                                         @RequestParam("timestamp") Long timestamp,
                                         @RequestParam("recvWindow") Long recvWindow);

    @GetMapping(value = "/api/v3/order")
    OrderInfoResponse getOrderInfo(@RequestParam("symbol") String symbol,
                                   @RequestParam("orderId") Long orderId,
                                   @RequestParam("timestamp") Long timestamp,
                                   @RequestParam("recvWindow") Long recvWindow);

    @PostMapping(value = "/api/v3/order")
    OrderInfoResponse newOrder(@RequestParam("symbol") String symbol,
                               @RequestParam("side") OrderSide side,
                               @RequestParam("type") OrderType type,
                               @RequestParam("timeInForce") TimeInForce timeInForce,
                               @RequestParam("quantity") String quantity,
                               @RequestParam("price") String price,
                               @RequestParam("timestamp") Long timestamp,
                               @RequestParam("recvWindow") Long recvWindow);

    @DeleteMapping(value = "/api/v3/order")
    OrderInfoResponse cancelOrder(@RequestParam("symbol") String symbol,
                                  @RequestParam("orderId") Long orderId,
                                  @RequestParam("timestamp") Long timestamp,
                                  @RequestParam("recvWindow") Long recvWindow);

    @GetMapping(value = "/api/v3/time")
    GetServerTimeResponse getServerTime();

    @PostMapping(value = "/sapi/v3/asset/getUserAsset")
    List<BalanceInfoResponse> getBalanceInfo(@RequestParam("asset") String asset,
                                             @RequestParam("timestamp") Long timestamp,
                                             @RequestParam("recvWindow") Long recvWindow);

    @GetMapping(value = "/api/v3/trades")
    List<TradeResponse> getTradeList(@RequestParam("symbol") String symbol,
                                     @RequestParam("limit") Integer limit);

    @GetMapping(value = "/api/v3/depth")
    OrderBookResponse getOrderBook(@RequestParam("symbol") String symbol,
                                   @RequestParam("limit") Integer limit);

}
