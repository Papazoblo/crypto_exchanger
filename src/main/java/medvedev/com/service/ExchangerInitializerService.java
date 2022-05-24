package medvedev.com.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.PriceHistoryBlockDto;
import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.ExchangeStrategyFactory;
import medvedev.com.service.exchangefactory.FiatCryptExchangeStrategy;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ExchangerInitializerService {

    private final SystemStateService stateService;
    private final ExchangeStrategyFactory exchangeStrategyFactory;

    public void initializeExchangeProcess(List<PriceHistoryBlockDto> priceBlocksHistory) {
        if (stateService.isSystemLaunched()) {
            try {
                ExchangeStrategy strategy = exchangeStrategyFactory.getExchangeStrategy(priceBlocksHistory);
                strategy.launchExchangeAlgorithm(createPriceHistoryFromBlock(priceBlocksHistory.get(0), strategy));
            } catch (Exception ex) {
                log.info(ex.getMessage(), ex.getCause());
            }
        }
    }

    private static PriceHistoryDto createPriceHistoryFromBlock(PriceHistoryBlockDto block, ExchangeStrategy strategy) {
        PriceHistoryDto ph = new PriceHistoryDto(block.getDateOpen(),
                strategy instanceof FiatCryptExchangeStrategy
                        ? new BigDecimalWrapper(block.getMin())
                        : new BigDecimalWrapper(block.getMax()),
                block.getAvgChangeType());
        log.info("Try to exchange with price = " + ph.getPrice());
        return ph;
    }
}
