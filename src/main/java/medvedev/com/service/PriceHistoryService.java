package medvedev.com.service;

import com.binance.api.client.domain.market.TickerStatistics;
import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.entity.PriceHistoryEntity;
import medvedev.com.repository.PriceHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public double[] getPriceHistoryList() {
        LocalDateTime localDateTime = timeService.now().minusDays(10);
        List<PriceHistoryEntity> entityList = repository.findAllByDateIsBefore(localDateTime);
        double[] values = new double[entityList.size()];
        for (int i = 0; i < entityList.size(); i++) {
            values[i] = Double.parseDouble(entityList.get(i).getPrice()) / 10000;
        }
        return values;
    }
}
