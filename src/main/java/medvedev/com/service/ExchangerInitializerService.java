package medvedev.com.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.ExchangeStrategyFactory;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ExchangerInitializerService {

    private final SystemStateService stateService;
    private final ExchangeStrategyFactory exchangeStrategyFactory;

    public void initializeExchangeProcess(PriceHistoryDto[] priceHistory) {
        if (stateService.isSystemLaunched()) {
            try {
                ExchangeStrategy strategy = exchangeStrategyFactory.getExchangeStrategy(priceHistory);
                strategy.launchExchangeAlgorithm(priceHistory[0]);
            } catch (Exception ex) {
                log.info(ex.getMessage(), ex.getCause());
            }
        }
    }
}
