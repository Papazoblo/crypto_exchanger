package medvedev.com.service;

import com.binance.api.client.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.exception.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStatusCheckerService {

    private final ExchangeHistoryService historyService;
    private final BinanceClient client;

    @Scheduled(cron = "")
    public void checkOrderStatus() {
        try {
            ExchangeHistoryDto record = historyService.getNewExchange();
            OrderStatus status = client.getOrderStatus(record.getOrderId());
            if (status != record.getOrderStatus()) {
                historyService.alterStatusById(record.getId(), status);
            }
        } catch (EntityNotFoundException ex) {
            //TODO лог в debug
        } catch (Exception ex) {
            //TODO лог в error
        }
    }
}
