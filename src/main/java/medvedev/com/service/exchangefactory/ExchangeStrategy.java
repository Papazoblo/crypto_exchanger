package medvedev.com.service.exchangefactory;

import medvedev.com.dto.PriceHistoryDto;

public interface ExchangeStrategy {

    void launchExchangeAlgorithm(PriceHistoryDto priceChange);
}
