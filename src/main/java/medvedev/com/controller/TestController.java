package medvedev.com.controller;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.enums.BlockTimeType;
import medvedev.com.service.CandleAnalyzerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final BinanceApiClient binanceApiClient;
    private final BinanceProperty property;
    private final CandleAnalyzerService candleAnalyzerService;

    @GetMapping("/test/check-candle/{id}/{type}")
    public void testStrategy(@PathVariable("id") Long id,
                             @PathVariable("type") BlockTimeType timeType) {
        candleAnalyzerService.test(id, timeType);
    }

    @GetMapping("/test/sell")
    public void testSell() {
        candleAnalyzerService.createSell();
    }

    @GetMapping("/test/message")
    public void testMessage() {
        candleAnalyzerService.createMessage();
    }

    @GetMapping("/test/{val}")
    public Object test(@PathVariable("val") Integer pos) {
        List<OrderInfoResponse> response = binanceApiClient.getAllOrders(property.getSymbol(), binanceApiClient.getServerTime().getServerTime(),
                5000L);
       /* BalanceInfoResponse balance = binanceApiClient.getBalanceInfo(Currency.USDT.name(),
                binanceApiClient.getServerTime().getServerTime(),
                property.getRectWindow()).get(0);
        System.out.println(balance);

        double balanceVal = (double) ((int) ((Double.parseDouble(balance.getFree()) * 10000)
                / (Double.parseDouble(binanceApiClient.getCurrentPrice(property.getSymbol()).getPrice()) * 10000)
                * 0.999 * 10000)) / 10000;
        System.out.println(balanceVal);


        OrderBookResponse response = binanceApiClient.getOrderBook(property.getSymbol(), 2000);
        Double price = Double.valueOf(binanceApiClient.getCurrentPrice(property.getSymbol()).getPrice());

        System.out.println("Продажи");
        Optional<Map.Entry<Integer, List<String[]>>> sellResult = response.getAsks().stream()
                .collect(groupingBy(item -> Integer.valueOf(item[PRICE_INDEX].substring(0, item[PRICE_INDEX].indexOf('.')))))
                .entrySet().stream()
                .filter(entry -> Math.abs(entry.getKey() - price) < pos)
                .max((o1, o2) -> Double.compare(o1.getValue().stream()
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum(), o2.getValue().stream()
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum()));
        sellResult.ifPresent(entry -> {

            entry.getValue().stream()
                    .max(Comparator.comparing(o -> Double.valueOf(o[QUANTITY_INDEX])))
                    .ifPresent(value -> System.out.println(value[PRICE_INDEX] + " " + value[QUANTITY_INDEX]));

            System.out.println(entry.getKey() + " " + entry.getValue().stream()
                    .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                    .sum());
        });
                *//*.sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach((entry) -> System.out.println(entry.getKey() + " " + entry.getValue().stream()
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum()));*//*

        System.out.println("Покупки");
        Optional<Map.Entry<Integer, List<String[]>>> buyResult = response.getBids().stream()
                .collect(groupingBy(item -> Integer.valueOf(item[PRICE_INDEX].substring(0, item[PRICE_INDEX].indexOf('.')))))
                .entrySet().stream()
                .filter(entry -> Math.abs(entry.getKey() - price) < pos)
                .max((o1, o2) -> Double.compare(o1.getValue().stream()
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum(), o2.getValue().stream()
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum()));
        buyResult.ifPresent(entry -> {

            entry.getValue().stream()
                    .max(Comparator.comparing(o -> Double.valueOf(o[QUANTITY_INDEX])))
                    .ifPresent(value -> System.out.println(value[PRICE_INDEX] + " " + value[QUANTITY_INDEX]));

            System.out.println(entry.getKey() + " " + entry.getValue().stream()
                    .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                    .sum());
        });
//                .sorted(Comparator.comparing(Map.Entry::getKey))
//                .forEach((entry) -> System.out.println(entry.getKey() + " " + entry.getValue().stream()
//                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
//                        .sum()));

        System.out.println(new StringBuilder()
                .append("Текущий курс: ")
                .append(binanceApiClient.getCurrentPrice(property.getSymbol()).getPrice())
                .append("\nВсего продаж: ")
                .append(response.getBids().stream()
                        .mapToDouble(value -> Double.parseDouble(value[QUANTITY_INDEX]))
                        .sum())
                .append(" [")
                .append(response.getBids().get(0)[PRICE_INDEX])
                .append(" -> ")
                .append(response.getBids().get(response.getBids().size() - 1)[PRICE_INDEX])
                .append("\nВсего покупки: ")
                .append(response.getAsks().stream()
                        .mapToDouble(value -> Double.parseDouble(value[QUANTITY_INDEX]))
                        .sum())
                .append(" [")
                .append(response.getAsks().get(0)[PRICE_INDEX])
                .append(" -> ")
                .append(response.getAsks().get(response.getAsks().size() - 1)[PRICE_INDEX])
                .append("\n\n\n"));
        *//*return binanceApiClient.getTradeList(property.getSymbol(), 1000).stream()
                .collect(Collectors.groupingBy(item -> Pair.of(item.getPrice(), item.getIsBuyerMaker())))
                .entrySet().stream()
                .map(entry -> {
                    TradeResponse tradeResponse = new TradeResponse();
                    tradeResponse.setPrice(entry.getKey().getFirst());
                    tradeResponse.setIsBuyerMaker(entry.getKey().getSecond());
                    tradeResponse.setQty(String.valueOf(entry.getValue().stream()
                            .mapToDouble(item -> BigDecimalWrapper.of(item.getQty()).longValue())
                            .sum() / BigDecimalWrapper.CONSTANT.intValue()));
                    return tradeResponse;
                })
                .sorted((o1, o2) -> o2.getPrice().compareTo(o1.getPrice()));*/
        return response;
    }

}
