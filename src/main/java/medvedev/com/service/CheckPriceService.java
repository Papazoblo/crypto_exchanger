package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CheckPriceService {

    //@Value("${fixed-rate.price-record-count}")
    private static final int recordCount = 10;

    private final BinanceClient client;
    private final PriceHistoryService priceHistoryService;
    private final ExchangerInitializerService exchangerInitializerService;

    @Scheduled(fixedRateString = "${fixed-rate.check-price}")
    public void checkPriceNormal() {
        PriceHistoryDto[] priceHistoryDtos = getPriceChange();
        if (priceHistoryDtos.length == 0) {
            log.info("Price history array is empty");
        } else {
            exchangerInitializerService.initializeExchangeProcess(priceHistoryDtos);
        }
    }

    private PriceHistoryDto[] getPriceChange() {
        TickerStatistics statistics = client.getPriceInfo();
        priceHistoryService.savePrice(new BigDecimalWrapper(statistics.getLastPrice()));
        return priceHistoryService.getFirstRecords(recordCount);
    }
}
