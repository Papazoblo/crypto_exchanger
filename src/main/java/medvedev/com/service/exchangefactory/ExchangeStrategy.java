package medvedev.com.service.exchangefactory;

import medvedev.com.dto.PriceChangeDto;

public interface ExchangeStrategy {

    void launchExchangeAlgorithm(PriceChangeDto priceChange);
}
