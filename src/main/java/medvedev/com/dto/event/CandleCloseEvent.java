package medvedev.com.dto.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CandleCloseEvent extends ApplicationEvent {

    private final Long blockId;

    public CandleCloseEvent(Long blockId, Object source) {
        super(source);
        this.blockId = blockId;
    }
}
