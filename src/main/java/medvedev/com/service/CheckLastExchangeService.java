package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.enums.SystemConfiguration;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckLastExchangeService {

    private final ExchangeHistoryService exchangeHistoryService;
    private final TimeService timeService;
    private final SystemConfigurationService systemConfigurationService;
    //private final BinanceClient client;

    //по идее мониторим ордеры совершенные через интерфейс
    //@Scheduled(cron = "${exchange.cron.check-last-exchange}")
    public void checkLastExchange() {
        LocalDateTime startTimeRange = timeService.nowMinusMinutes(systemConfigurationService
                .findIntegerByName(SystemConfiguration.AVAILABLE_MINUTES_COUNT_WITHOUT_EXCHANGE));

        exchangeHistoryService.getLastExchange().ifPresentOrElse(exchange -> {
            if (exchange.getDateTime().isBefore(startTimeRange)) {
                checkLastOrder();
            }
        }, this::checkLastOrder);
    }

    private void checkLastOrder() {
        try {
            //Order order = client.getLastFilledOrder();
            //exchangeHistoryService.saveIfNotExist(order);
        } catch (Exception ex) {
            log.debug("Order list is empty");
        }
    }
}
