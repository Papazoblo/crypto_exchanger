package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.ExchangeStrategyFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CheckPriceService {

    private final BinanceClient client;
    private final PriceChangeService priceChangeService;
    private final SystemStateService stateService;
    private final ExchangeStrategyFactory exchangeStrategyFactory;
    private final CheckPredictionPriceService checkPredictionPriceService;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void checkPrice() throws InterruptedException {

        getPriceChange();
        Thread.sleep(5 * 1000);
        PriceChangeDto priceChange = getPriceChange();

        if (stateService.isSystemNotLaunched()) {
            return;
        }

        try {
            ExchangeStrategy strategy = exchangeStrategyFactory.getExchangeStrategy(priceChange);
            strategy.launchExchangeAlgorithm(priceChange);
            //checkPredictionPriceService.checkPrediction(priceChange, strategy);
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
    }

    private PriceChangeDto getPriceChange() {
        TickerStatistics statistics = client.getPriceInfo();
        return priceChangeService.refresh(statistics);
    }
}
