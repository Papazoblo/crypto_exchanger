package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.CheckPriceType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CheckPriceService {

    private static final long RECHECK_PRICE_CHANGE = 10 * 60 * 1000;
    private static final long RECHECK_PRICE_CHANGE_QUICK = 15 * 1000;

    private final BinanceClient client;
    private final PriceChangeService priceChangeService;
    private final ExchangerInitializerService exchangerInitializerService;

    @Scheduled(fixedRate = 25 * 60 * 1000)
    public void checkPriceNormal() {
        new Thread(() -> {
            try {
                checkPrice(CheckPriceType.NORMAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Scheduled(fixedRate = 30 * 1000)
    public void quicklyCheckPrice() {
        new Thread(() -> {
            try {
                checkPrice(CheckPriceType.QUICK);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void checkPrice(CheckPriceType checkPriceType) throws InterruptedException {
        PriceChangeDto priceChangeOne;
        PriceChangeDto priceChangeTwo;
        priceChangeOne = getPriceChange(checkPriceType);
        if (checkPriceType == CheckPriceType.QUICK) {
            Thread.sleep(RECHECK_PRICE_CHANGE_QUICK);
        } else {
            Thread.sleep(RECHECK_PRICE_CHANGE);
        }
        priceChangeTwo = getPriceChange(checkPriceType);
        exchangerInitializerService.initializeExchangeProcess(priceChangeOne, priceChangeTwo, checkPriceType);
    }

    private PriceChangeDto getPriceChange(CheckPriceType checkPriceType) {
        TickerStatistics statistics = client.getPriceInfo();
        return priceChangeService.refresh(statistics, checkPriceType);
    }
}
