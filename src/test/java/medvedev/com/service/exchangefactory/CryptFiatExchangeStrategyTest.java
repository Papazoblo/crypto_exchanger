package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.TickerStatistics;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE;
import static org.mockito.Mockito.*;

public class CryptFiatExchangeStrategyTest {

    private BinanceClient client;
    private ExchangeHistoryService exchangeHistoryService;
    private SystemConfigurationService systemConfigurationService;
    private CryptFiatExchangeStrategy strategy;

    @BeforeEach
    void setUp() {
        client = mock(BinanceClient.class);
        exchangeHistoryService = mock(ExchangeHistoryService.class);
        systemConfigurationService = mock(SystemConfigurationService.class);
        strategy = new CryptFiatExchangeStrategy(client, exchangeHistoryService,
                systemConfigurationService);
    }

    @Nested
    class LaunchExchangeAlgorithm {

        private BigDecimalWrapper lastPrice;

        @BeforeEach
        void setUp() {
            String lastPriceString = "1000";
            TickerStatistics statistics = new TickerStatistics();
            statistics.setLastPrice(lastPriceString);
            lastPrice = new BigDecimalWrapper(lastPriceString);

            when(client.getPriceInfo()).thenReturn(statistics);
        }

        @Nested
        class ExchangeWithDifferencePrice {

            private Double minDifferencePrice;
            private PriceChangeDto priceChangeDto;
            private AssetBalance balance;

            @BeforeEach
            void setUp() {
                minDifferencePrice = 5.0;
                priceChangeDto = new PriceChangeDto();
                balance = new AssetBalance();
                balance.setFree("1.0");

                when(systemConfigurationService.findDoubleByName(MIN_DIFFERENCE_PRICE))
                        .thenReturn(minDifferencePrice);
            }

            @Test
            void shouldNotExchange() {

                when(exchangeHistoryService.getOpenProfitableExchange(lastPrice))
                        .thenReturn(Collections.emptyList());
                when(client.getBalanceByCurrency(Currency.ETH)).thenReturn(balance);

                strategy.launchExchangeAlgorithm(priceChangeDto);
                verify(client, never()).createSellOrder(any());
            }

            @Nested
            class MustBeExchange {

                @Test
                void shouldReturnWhenListIsNotEmpty() {
                    List<ExchangeHistoryDto> profitableList = Arrays.asList(
                            generateHistoryDto("990"),
                            generateHistoryDto("1000"),
                            generateHistoryDto("1050"),
                            generateHistoryDto("1100")
                    );

                    when(exchangeHistoryService.getOpenProfitableExchange(lastPrice))
                            .thenReturn(profitableList);
                }
            }
        }
    }

    private static ExchangeHistoryDto generateHistoryDto(String price) {
        return new ExchangeHistoryDto(null, null, OrderSide.SELL, LocalDateTime.now(),
                null, null, new BigDecimalWrapper(price),
                OrderStatus.FILLED, null);
    }
}
