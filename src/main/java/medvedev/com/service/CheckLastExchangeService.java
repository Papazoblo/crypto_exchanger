package medvedev.com.service;

import com.binance.api.client.domain.account.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceClient;
import medvedev.com.enums.SystemConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckLastExchangeService {

    private final ExchangeHistoryService exchangeHistoryService;
    private final TimeService timeService;
    private final SystemConfigurationService systemConfigurationService;
    private final BinanceClient client;

    @Scheduled(cron = "${exchange.cron.check-last-exchange}")
    public void checkLastExchange() {
        LocalDateTime startTimeRange = timeService.nowMinusMinutes(systemConfigurationService
                .findIntegerByName(SystemConfiguration.AVAILABLE_MINUTES_COUNT_WITHOUT_EXCHANGE));

        exchangeHistoryService.getLastExchange().ifPresentOrElse(exchange -> {
            if (exchange.getDateTime().isBefore(startTimeRange)) {
                checkLastOrder(startTimeRange);
            }
        }, () -> checkLastOrder(startTimeRange));
    }

    private void checkLastOrder(LocalDateTime startTimeRange) {
        try {
            Order order = client.getLastFilledOrder();
            LocalDateTime orderTime = new Timestamp(order.getTime()).toLocalDateTime();
            if (orderTime.isAfter(startTimeRange)) {
                exchangeHistoryService.saveIfNotExist(order);
            }
        } catch (Exception ex) {
            log.debug("Order list is empty");
        }
    }
}
