package medvedev.com.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import medvedev.com.client.BinanceApiClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimestampComponent {

    private final BinanceApiClient binanceApiClient;
    private Long serverTimeDifference;

    @EventListener(ApplicationReadyEvent.class)
    public void applicationStartEvent() {
        Long localTime1 = Timestamp.valueOf(LocalDateTime.now()).getTime();
        Long serverTime = binanceApiClient.getServerTime().getServerTime();
        serverTimeDifference = localTime1 - serverTime;
        log.info(localTime1 + " " + serverTime + " " + serverTimeDifference);
    }

    public Long getTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now()).getTime() - (serverTimeDifference < 0 ? Math.abs(serverTimeDifference) : serverTimeDifference);
    }
}
