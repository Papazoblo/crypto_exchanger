package medvedev.com.component;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceApiClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TimestampComponent {

    private final BinanceApiClient binanceApiClient;
    private Long serverTimeDifference;

    @EventListener(ApplicationReadyEvent.class)
    public void applicationStartEvent() {
        Long serverTime = binanceApiClient.getServerTime().getServerTime();
        Long localTime = Timestamp.valueOf(LocalDateTime.now()).getTime();
        serverTimeDifference = localTime - serverTime;
    }

    public Long getTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now()).getTime() + serverTimeDifference;
    }
}
