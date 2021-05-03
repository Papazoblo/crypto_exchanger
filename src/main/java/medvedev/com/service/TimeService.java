package medvedev.com.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TimeService {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    public LocalDateTime nowMinusMinutes(Integer minutes) {
        return now().minusMinutes(minutes);
    }
}
