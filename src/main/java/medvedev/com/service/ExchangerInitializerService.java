package medvedev.com.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.CheckPriceType;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.ExchangeStrategyFactory;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ExchangerInitializerService {

    private final SystemStateService stateService;
    private final ExchangeStrategyFactory exchangeStrategyFactory;

    public void initializeExchangeProcess(PriceChangeDto priceChangeOne, PriceChangeDto priceChangeTwo,
                                          CheckPriceType checkPriceType) {
        if (stateService.isSystemLaunched()) {
            try {
                ExchangeStrategy strategy = exchangeStrategyFactory.getExchangeStrategy(priceChangeOne, priceChangeTwo,
                        checkPriceType);
                strategy.launchExchangeAlgorithm(priceChangeTwo);
            } catch (Exception ex) {
                log.info(ex.getMessage());
            }
        }
    }
}
