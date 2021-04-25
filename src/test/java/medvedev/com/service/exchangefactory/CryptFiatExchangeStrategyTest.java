package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.market.TickerStatistics;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.enums.Currency;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class CryptFiatExchangeStrategyTest {

    private BinanceClient client;
    private ExchangeHistoryService exchangeHistoryService;
    private CheckPriceDifferenceService checkPriceDifferenceService;
    private TelegramPollingService telegramPollingService;
    private CryptFiatExchangeStrategy strategy;

    @BeforeEach
    void setUp() {
        client = mock(BinanceClient.class);
        exchangeHistoryService = mock(ExchangeHistoryService.class);
        checkPriceDifferenceService = mock(CheckPriceDifferenceService.class);
        telegramPollingService = mock(TelegramPollingService.class);
        strategy = new CryptFiatExchangeStrategy(client, exchangeHistoryService, telegramPollingService,
                checkPriceDifferenceService);
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
                priceChangeDto.setNewPrice(new BigDecimalWrapper("1020"));
                balance = new AssetBalance();
                balance.setFree("1.0");

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
                    String sumToExchange = "1.96";
                    String price1 = "990";
                    String price2 = "1000";
                    String price3 = "1100";
                    BigDecimalWrapper lastPrice = new BigDecimalWrapper(1020);
                    AssetBalance balance = new AssetBalance();
                    balance.setFree("2");
                    NewOrderResponse response = new NewOrderResponse();
                    response.setClientOrderId("1");
                    response.setCummulativeQuoteQty("1");
                    response.setExecutedQty("1");
                    response.setPrice("1");
                    response.setTransactTime(1619337129000L);
                    ExchangeHistoryDto historyDto = new ExchangeHistoryDto(
                            1L, 2L, OrderSide.SELL, LocalDateTime.now(), new BigDecimalWrapper(1),
                            new BigDecimalWrapper(2), new BigDecimalWrapper(3), OrderStatus.FILLED,
                            null);

                    List<ExchangeHistoryDto> profitableList = Arrays.asList(
                            generateHistoryDto(price1),
                            generateHistoryDto(price2),
                            generateHistoryDto(price3)
                    );

                    when(exchangeHistoryService.getOpenProfitableExchange(lastPrice))
                            .thenReturn(profitableList);
                    when(checkPriceDifferenceService.isPriceIncreased(lastPrice,
                            Double.parseDouble(price1))).thenReturn(true);
                    when(checkPriceDifferenceService.isPriceIncreased(lastPrice,
                            Double.parseDouble(price2))).thenReturn(true);
                    when(checkPriceDifferenceService.isPriceIncreased(lastPrice,
                            Double.parseDouble(price3))).thenReturn(false);
                    when(client.getBalanceByCurrency(Currency.ETH))
                            .thenReturn(balance);
                    when(client.createSellOrder(any()))
                            .thenReturn(response);
                    when(exchangeHistoryService.save(any())).thenReturn(historyDto);

                    strategy.launchExchangeAlgorithm(priceChangeDto);
                    verify(exchangeHistoryService).closingOpenedExchangeById(any(), any());
                    verify(telegramPollingService).sendMessage(any());
                }
            }
        }
    }

    private static ExchangeHistoryDto generateHistoryDto(String price) {
        return new ExchangeHistoryDto(null, null, OrderSide.SELL, LocalDateTime.now(),
                null, new BigDecimalWrapper(1), new BigDecimalWrapper(price),
                OrderStatus.FILLED, null);
    }
}
