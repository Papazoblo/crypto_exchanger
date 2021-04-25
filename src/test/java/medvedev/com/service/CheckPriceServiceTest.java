package medvedev.com.service;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.TickerStatistics;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
import medvedev.com.exception.NoSuitableStrategyException;
import medvedev.com.service.exchangefactory.CryptFiatExchangeStrategy;
import medvedev.com.service.exchangefactory.ExchangeStrategy;
import medvedev.com.service.exchangefactory.ExchangeStrategyFactory;
import medvedev.com.service.telegram.TelegramPollingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class CheckPriceServiceTest {

    private ExchangeHistoryService exchangeHistoryService;
    private CheckPriceDifferenceService checkPriceDifferenceService;
    private TelegramPollingService telegramPollingService;
    private BinanceClient client;
    private PriceChangeService priceChangeService;
    private SystemStateService systemStateService;
    private ExchangeStrategyFactory strategyFactory;
    private CheckPriceService service;

    @BeforeEach
    void setUp() {
        exchangeHistoryService = mock(ExchangeHistoryService.class);
        checkPriceDifferenceService = mock(CheckPriceDifferenceService.class);
        telegramPollingService = mock(TelegramPollingService.class);
        client = mock(BinanceClient.class);
        priceChangeService = mock(PriceChangeService.class);
        systemStateService = mock(SystemStateService.class);
        strategyFactory = mock(ExchangeStrategyFactory.class);
        service = new CheckPriceService(client, priceChangeService, systemStateService,
                strategyFactory);
    }

    @Nested
    class CheckPrice {

        private TickerStatistics tickerStatistics;
        private PriceChangeDto priceChangeDto;

        @BeforeEach
        void setUp() {
            tickerStatistics = new TickerStatistics();
            priceChangeDto = new PriceChangeDto();

            when(client.getPriceInfo()).thenReturn(tickerStatistics);
            when(priceChangeService.refresh(tickerStatistics)).thenReturn(priceChangeDto);
        }

        @Test
        void shouldReturnWhenSystemOffline() {

            when(systemStateService.isSystemNotLaunched()).thenReturn(true);

            service.checkPrice();
            verify(strategyFactory, never()).getExchangeStrategy(priceChangeDto);
        }

        @Test
        void shouldLaunchExchangeStrategy() {
            ExchangeStrategy strategy = new CryptFiatExchangeStrategy(client, exchangeHistoryService,
                    telegramPollingService, checkPriceDifferenceService);
            AssetBalance balance = new AssetBalance();
            balance.setFree("0.0");

            when(systemStateService.isSystemNotLaunched()).thenReturn(false);
            when(strategyFactory.getExchangeStrategy(priceChangeDto)).thenReturn(strategy);
            when(client.getBalanceByCurrency(Currency.ETH))
                    .thenReturn(balance);

            service.checkPrice();
            verify(strategyFactory).getExchangeStrategy(priceChangeDto);
        }

        @Test
        void shouldThrowException() {

            when(systemStateService.isSystemNotLaunched()).thenReturn(false);
            when(strategyFactory.getExchangeStrategy(priceChangeDto))
                    .thenThrow(NoSuitableStrategyException.class);

            service.checkPrice();
        }
    }
}
