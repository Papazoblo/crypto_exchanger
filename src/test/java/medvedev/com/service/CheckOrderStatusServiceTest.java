package medvedev.com.service;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

public class CheckOrderStatusServiceTest {

    private SystemStateService systemStateService;
    private ExchangeHistoryService exchangeHistoryService;
    private BinanceClient binanceClient;
    private CheckOrderStatusService service;

    @BeforeEach
    void setUp() {
        systemStateService = mock(SystemStateService.class);
        exchangeHistoryService = mock(ExchangeHistoryService.class);
        binanceClient = mock(BinanceClient.class);
        service = new CheckOrderStatusService(systemStateService, exchangeHistoryService, binanceClient);
    }

    @Nested
    class CheckOrderStatus {

        private ExchangeHistoryDto historyDto;

        @BeforeEach
        void setUp() {
            historyDto = generateExchangeHistoryDto();

            when(systemStateService.isSystemNotLaunched())
                    .thenReturn(false);
        }

        @Test
        void shouldNotChangeStatusWhenSystemStopped() {

            when(systemStateService.isSystemNotLaunched())
                    .thenReturn(true);

            service.checkOrderStatus();
            verify(exchangeHistoryService, never()).getNewExchange();
            verify(binanceClient, never()).getOrderStatus(any());
            verify(exchangeHistoryService, never()).alterStatusById(any(), any());
        }

        @Test
        void shouldChangeStatus() {
            OrderStatus newStatus = OrderStatus.FILLED;

            when(exchangeHistoryService.getNewExchange()).thenReturn(historyDto);
            when(binanceClient.getOrderStatus(historyDto.getOrderId()))
                    .thenReturn(newStatus);

            service.checkOrderStatus();
            verify(exchangeHistoryService).alterStatusById(historyDto.getId(), newStatus);
        }

        @Test
        void shouldNotChangeStatus() {
            OrderStatus newStatus = OrderStatus.NEW;

            when(exchangeHistoryService.getNewExchange()).thenReturn(historyDto);
            when(binanceClient.getOrderStatus(historyDto.getOrderId()))
                    .thenReturn(newStatus);

            service.checkOrderStatus();
            verify(exchangeHistoryService, never()).alterStatusById(historyDto.getId(), newStatus);
        }

        @Test
        void shouldThrowEntityNotFoundException() {

            when(exchangeHistoryService.getNewExchange())
                    .thenThrow(new EntityNotFoundException("New exchange history"));

            service.checkOrderStatus();
            verify(binanceClient, never()).getOrderStatus(any());
            verify(exchangeHistoryService, never()).alterStatusById(any(), any());

        }

        @Test
        void shouldThrowAnotherException() {
            OrderStatus newStatus = OrderStatus.FILLED;

            when(exchangeHistoryService.getNewExchange()).thenReturn(historyDto);
            when(binanceClient.getOrderStatus(historyDto.getOrderId()))
                    .thenReturn(newStatus);
            doThrow(SQLIntegrityConstraintViolationException.class)
                    .when(exchangeHistoryService).alterStatusById(historyDto.getId(), newStatus);

            service.checkOrderStatus();
        }
    }

    private static ExchangeHistoryDto generateExchangeHistoryDto() {
        return new ExchangeHistoryDto(
                10L, 10L, OrderSide.BUY, LocalDateTime.now(), new BigDecimalWrapper(100),
                new BigDecimalWrapper(200), new BigDecimalWrapper(2), OrderStatus.NEW, null
        );
    }
}
