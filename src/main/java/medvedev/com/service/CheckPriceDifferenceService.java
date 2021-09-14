package medvedev.com.service;

import com.binance.api.client.domain.OrderSide;
import lombok.RequiredArgsConstructor;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE;
import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE_FIAT_CRYPT;

@Service
@RequiredArgsConstructor
public class CheckPriceDifferenceService {

    private static final int HUNDRED = 100;

    private final SystemConfigurationService systemConfigurationService;
    private final ExchangeHistoryService exchangeHistoryService;

    public boolean isPriceIncreased(BigDecimalWrapper lastPrice, double recordPrice) {
        double lastPriceInDouble = lastPrice.doubleValue();
        return -((recordPrice * HUNDRED / lastPriceInDouble) - HUNDRED) >
                systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE);
    }

    public boolean isPriceDecreased(BigDecimalWrapper lastPrice, double recordPrice) {
        double lastPriceInDouble = lastPrice.doubleValue();
        return (recordPrice * HUNDRED / lastPriceInDouble) - HUNDRED >
                systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE_FIAT_CRYPT);
    }

    public String getPriceToExchange() {
        return exchangeHistoryService.getLastExchange().map(exchange -> {
            double price = exchange.getPrice().doubleValue();
            if (exchange.getOrderType() == OrderSide.BUY) {
                return String.valueOf(price + (price *
                        systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE) / HUNDRED)
                );
            } else {
                return String.valueOf(price - (price *
                        systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE_FIAT_CRYPT) / HUNDRED)
                );
            }
        })
                .orElse("Failed to calculate the price for a possible exchange");
    }
}
