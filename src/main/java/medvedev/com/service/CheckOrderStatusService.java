package medvedev.com.service;

import com.binance.api.client.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.service.telegram.TelegramPollingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckOrderStatusService {

    private final SystemStateService stateService;
    private final ExchangeHistoryService historyService;
    private final BinanceClient client;
    private final TelegramPollingService telegramPollingService;

    //вроде как закрывает открытые ордеры
    //@Scheduled(cron = "${exchange.cron.check-status-order}")
    public void updateOrderStatus() {
        if (stateService.isSystemLaunched()) {
            try {
                ExchangeHistoryDto record = historyService.getNewExchange();
                OrderStatus status = client.getOrderStatus(record.getOrderId());
                if (status != record.getOrderStatus()) {
                    historyService.alterStatusById(record.getId(), status);
                    if (status == OrderStatus.FILLED) {
                        historyService.closingOpenedExchangeById(record.getId());
                        telegramPollingService.sendMessage("Exchange completed");
                    }
                }
                //отменяем ордеры старше 12 часов
                if (record.getDateTime().plusHours(12L).isBefore(LocalDateTime.now())) {
                    //client.cancelOrder(record);
                }
            } catch (EntityNotFoundException ex) {
                log.debug(ex);
            } catch (Exception ex) {
                log.error(ex);
            }
        }
    }
}
