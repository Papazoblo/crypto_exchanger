package medvedev.com.dto.event;

import lombok.Getter;
import medvedev.com.enums.BlockTimeType;
import org.springframework.context.ApplicationEvent;

@Getter
public class CandleCloseEvent extends ApplicationEvent {

    private final BlockTimeType timeType;

    public CandleCloseEvent(BlockTimeType timeType, Object source) {
        super(source);
        this.timeType = timeType;
    }
}
