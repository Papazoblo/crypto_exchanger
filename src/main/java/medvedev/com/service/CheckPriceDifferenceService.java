package medvedev.com.service;

import com.binance.api.client.domain.OrderSide;
import lombok.RequiredArgsConstructor;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE;

@Service
@RequiredArgsConstructor
public class CheckPriceDifferenceService {

    private static final int HUNDRED = 100;

    private final SystemConfigurationService systemConfigurationService;
    private final ExchangeHistoryService exchangeHistoryService;

    public boolean isPriceIncreased(BigDecimalWrapper curPrice, double oldPrice) {
        double curPriceInDouble = curPrice.doubleValue();
        return -((oldPrice * HUNDRED / curPriceInDouble) - HUNDRED) >
                systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE);
    }

    //если текущий курс упал
    public boolean isPriceDecreased(BigDecimalWrapper curPrice, double oldPrice) {
        double curPriceInDouble = curPrice.doubleValue();
        return (oldPrice * HUNDRED / curPriceInDouble) - HUNDRED >
                systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE);
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
                        systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE) / HUNDRED)
                );
            }
        })
                .orElse("Failed to calculate the price for a possible exchange");
    }
}
