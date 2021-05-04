package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.entity.PriceHistoryEntity;
import medvedev.com.repository.PriceHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository repository;
    private final BinanceClient client;
    private final TimeService timeService;

    @Scheduled(cron = "${exchange.cron.price-history}")
    public void savePrice() {

        TickerStatistics statistics = client.getPriceInfo();
        repository.save(new PriceHistoryEntity(
                timeService.now(),
                statistics.getLastPrice()
        ));
    }
}
