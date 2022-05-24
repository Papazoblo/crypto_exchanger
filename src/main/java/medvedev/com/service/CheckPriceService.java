package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CheckPriceService {

    private final BinanceClient client;
    private final PriceHistoryService priceHistoryService;

    @Scheduled(fixedRateString = "${fixed-rate.check-price}")
    public void checkPriceNormal() {
        TickerStatistics statistics = client.getPriceInfo();
        priceHistoryService.savePrice(new BigDecimalWrapper(statistics.getLastPrice()));
    }
}
