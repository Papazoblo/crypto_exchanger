package medvedev.com.controller;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.OrderBookResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static medvedev.com.dto.response.OrderBookResponse.PRICE_INDEX;
import static medvedev.com.dto.response.OrderBookResponse.QUANTITY_INDEX;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final BinanceApiClient binanceApiClient;
    private final BinanceProperty property;

    @GetMapping("/test/{val}")
    public Object test(@PathVariable("val") Integer pos) {
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
                /*.sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach((entry) -> System.out.println(entry.getKey() + " " + entry.getValue().stream()
                        .mapToDouble(item -> Double.parseDouble(item[QUANTITY_INDEX]))
                        .sum()));*/

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
        /*return binanceApiClient.getTradeList(property.getSymbol(), 1000).stream()
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
