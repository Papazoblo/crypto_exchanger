package medvedev.com.service;

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
}
