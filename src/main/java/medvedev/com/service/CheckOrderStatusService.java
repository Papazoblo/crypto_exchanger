package medvedev.com.service;

import com.binance.api.client.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.exception.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckOrderStatusService {

    private final SystemStateService stateService;
    private final ExchangeHistoryService historyService;
    private final BinanceClient client;

    @Scheduled(cron = "${exchange.cron.check-status-order}")
    public void checkOrderStatus() {

        if (stateService.isSystemNotLaunched()) {
            return;
        }

        try {
            ExchangeHistoryDto record = historyService.getNewExchange();
            OrderStatus status = client.getOrderStatus(record.getOrderId());
            if (status != record.getOrderStatus()) {
                historyService.alterStatusById(record.getId(), status);
            }
        } catch (EntityNotFoundException ex) {
            log.debug(ex);
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}
