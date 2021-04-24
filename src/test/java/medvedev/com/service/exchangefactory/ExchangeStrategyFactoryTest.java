package medvedev.com.service.exchangefactory;

import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.HavePriceChangeState;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.exception.NoSuitableStrategyException;
import medvedev.com.service.BalanceCheckerService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.telegram.TelegramPollingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ExchangeStrategyFactoryTest {

    private ExchangeHistoryService historyService;
    private BalanceCheckerService balanceCheckerService;
    private SystemConfigurationService systemConfigurationService;
    private TelegramPollingService telegramPollingService;
    private BinanceClient binanceClient;
    private ExchangeStrategyFactory factory;

    @BeforeEach
    void setUp() {
        historyService = mock(ExchangeHistoryService.class);
        balanceCheckerService = mock(BalanceCheckerService.class);
        systemConfigurationService = mock(SystemConfigurationService.class);
        telegramPollingService = mock(TelegramPollingService.class);
        binanceClient = mock(BinanceClient.class);
        factory = new ExchangeStrategyFactory(historyService, balanceCheckerService,
                systemConfigurationService, telegramPollingService, binanceClient);
    }

    @Nested
    class GetExchangeStrategy {

        private PriceChangeDto priceChange;

        @BeforeEach
        void setUp() {
            priceChange = new PriceChangeDto();
            priceChange.setHavePriceChangeState(HavePriceChangeState.WITH_CHANGES);
        }

        @Test
        void shouldReturnFiatCryptStrategy() {
            priceChange.setState(PriceChangeState.INCREASED);

            ExchangeStrategy actual = factory.getExchangeStrategy(priceChange);
            assertTrue(actual instanceof FiatCryptExchangeStrategy);
        }

        @Test
        void shouldReturnCryptFiatStrategy() {
            priceChange.setState(PriceChangeState.DECREASED);

            ExchangeStrategy actual = factory.getExchangeStrategy(priceChange);
            assertTrue(actual instanceof CryptFiatExchangeStrategy);
        }

        @Test
        void shouldThrowException() {
            priceChange.setHavePriceChangeState(HavePriceChangeState.WITHOUT_CHANGES);

            assertThrows(NoSuitableStrategyException.class, () -> factory.getExchangeStrategy(priceChange));
        }
    }
}
