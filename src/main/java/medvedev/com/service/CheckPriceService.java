package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CheckPriceService {

    private static final long RECHECK_PRICE_CHANGE = 5 * 60 * 1000;
    private static final int CHECK_PRICE_COUNT = 2;

    private final BinanceClient client;
    private final PriceChangeService priceChangeService;
    private final ExchangerInitializerService exchangerInitializerService;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void checkPrice() throws InterruptedException {

        PriceChangeDto priceChange = null;
        for (int i = 0; i < CHECK_PRICE_COUNT; i++) {
            if (i != 0) {
                Thread.sleep(RECHECK_PRICE_CHANGE);
            }
            TickerStatistics statistics = client.getPriceInfo();
            priceChange = priceChangeService.refresh(statistics);
        }
        exchangerInitializerService.initializeExchangeProcess(priceChange);
    }
}
