package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrderResponse;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.PriceChangeDto;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.Currency;
import medvedev.com.service.BalanceCheckerService;
import medvedev.com.service.CheckPriceDifferenceService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class FiatCryptExchangeStrategyTest {

    private BalanceCheckerService balanceCheckerService;
    private ExchangeHistoryService exchangeHistoryService;
    private BinanceClient binanceClient;
    private TelegramPollingService telegramPollingService;
    private CheckPriceDifferenceService checkPriceDifferenceService;
    private FiatCryptExchangeStrategy strategy;

    @BeforeEach
    void setUp() {
        balanceCheckerService = mock(BalanceCheckerService.class);
        exchangeHistoryService = mock(ExchangeHistoryService.class);
        binanceClient = mock(BinanceClient.class);
        telegramPollingService = mock(TelegramPollingService.class);
        checkPriceDifferenceService = mock(CheckPriceDifferenceService.class);
        strategy = new FiatCryptExchangeStrategy(balanceCheckerService, binanceClient, exchangeHistoryService,
                telegramPollingService, checkPriceDifferenceService);
    }

    @Nested
    class LaunchExchangeAlgorithm {

        private PriceChangeDto priceChange;
        private AssetBalance balance;
        private BigDecimalWrapper exchangeAmount;
        private BigDecimalWrapper convertedValue;
        private NewOrderResponse response;

        @BeforeEach
        void setUp() {

            priceChange = new PriceChangeDto();

            balance = new AssetBalance();
            balance.setFree("1000");
            exchangeAmount = new BigDecimalWrapper("1000");
            convertedValue = new BigDecimalWrapper("1.00000000");
            response = new NewOrderResponse();
            response.setClientOrderId("1");
            response.setCummulativeQuoteQty("1");
            response.setExecutedQty("1");
            response.setPrice("1");
            response.setTransactTime(1619337129000L);

            when(binanceClient.getBalanceByCurrency(Currency.USDT)).thenReturn(balance);
            when(balanceCheckerService.isEnoughFundsBalance(balance.getFree())).thenReturn(exchangeAmount);
        }

        @Test
        void shouldNotExchange() {

            priceChange.setNewPrice(new BigDecimalWrapper(900000));

            strategy.launchExchangeAlgorithm(priceChange);
            verify(exchangeHistoryService, never()).isNotExistExchangeSell();
        }

        @Test
        void shouldSendExchangeRequest() {
            priceChange.setNewPrice(new BigDecimalWrapper("1000"));

            when(exchangeHistoryService.isNotExistExchangeSell()).thenReturn(true);
            when(binanceClient.createSellOrder(any(BigDecimal.class))).thenReturn(response);


            strategy.launchExchangeAlgorithm(priceChange);
            verify(telegramPollingService).sendMessage(anyString());
            verify(exchangeHistoryService, never()).findLastSellExchange();
            verify(exchangeHistoryService).save(ExchangeHistoryEntity.from(response));
        }

        @Test
        void shouldDoExchange() {

            priceChange.setNewPrice(new BigDecimalWrapper("1000"));
            ExchangeHistoryDto historyDto = new ExchangeHistoryDto(null, null, null,
                    null, null, null, new BigDecimalWrapper("1000"), null,
                    null);

            when(exchangeHistoryService.isNotExistExchangeSell()).thenReturn(false);
            when(binanceClient.createSellOrder(any(BigDecimal.class))).thenReturn(response);
            when(exchangeHistoryService.findLastSellExchange()).thenReturn(historyDto);
            when(checkPriceDifferenceService.isPriceDecreased(priceChange.getNewPrice(),
                    historyDto.getPrice().doubleValue())).thenReturn(true);

            strategy.launchExchangeAlgorithm(priceChange);
            verify(telegramPollingService).sendMessage(anyString());
            verify(exchangeHistoryService).save(ExchangeHistoryEntity.from(response));
        }

        @Test
        void shouldDoNotExchange() {

            priceChange.setNewPrice(new BigDecimalWrapper("1000"));
            ExchangeHistoryDto historyDto = new ExchangeHistoryDto(null, null, null,
                    null, null, null, new BigDecimalWrapper("1000"), null,
                    null);

            when(exchangeHistoryService.isNotExistExchangeSell()).thenReturn(false);
            when(binanceClient.createSellOrder(any(BigDecimal.class))).thenReturn(response);
            when(exchangeHistoryService.findLastSellExchange()).thenReturn(historyDto);
            when(checkPriceDifferenceService.isPriceDecreased(priceChange.getNewPrice(),
                    historyDto.getPrice().doubleValue())).thenReturn(false);

            strategy.launchExchangeAlgorithm(priceChange);
            verify(telegramPollingService, never()).sendMessage(anyString());
            verify(exchangeHistoryService, never()).save(ExchangeHistoryEntity.from(response));
        }
    }
}
