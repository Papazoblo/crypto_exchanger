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

    @Scheduled(cron = "${exchange.cron.check-price}")
    public void checkPrice() {

        TickerStatistics statistics = client.getPriceInfo();
        PriceChangeDto priceChange = priceChangeService.refresh(statistics);

        if (stateService.isSystemNotLaunched()) {
            return;
        }

        try {
            ExchangeStrategy strategy = exchangeStrategyFactory.getExchangeStrategy(priceChange);
            strategy.launchExchangeAlgorithm(priceChange);
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
    }
}
