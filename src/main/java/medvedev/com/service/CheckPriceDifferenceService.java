package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE;
import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE_FIAT_CRYPT;

@Service
@RequiredArgsConstructor
public class CheckPriceDifferenceService {

    private final SystemConfigurationService systemConfigurationService;

    /**
     * @param lastPrice   2500
     * @param recordPrice 2000
     * @return
     */
    public boolean isPriceIncreased(BigDecimalWrapper lastPrice, double recordPrice) {
        double lastPriceInDouble = lastPrice.doubleValue();
        return -((recordPrice * 100 / lastPriceInDouble) - 100) > getPriceDifference(MIN_DIFFERENCE_PRICE);
    }

    /**
     * @param lastPrice   1500
     * @param recordPrice 2000
     * @return
     */
    public boolean isPriceDecreased(BigDecimalWrapper lastPrice, double recordPrice) {
        double lastPriceInDouble = lastPrice.doubleValue();
        return (recordPrice * 100 / lastPriceInDouble) - 100 > getPriceDifference(MIN_DIFFERENCE_PRICE_FIAT_CRYPT);
    }

    private double getPriceDifference(SystemConfiguration configuration) {
        return systemConfigurationService.findDoubleByName(configuration);
    }
}
