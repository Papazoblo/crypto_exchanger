package medvedev.com.dto.event;

import lombok.Getter;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.context.ApplicationEvent;

@Getter
public class PriceChangeEvent extends ApplicationEvent {

    private final BigDecimalWrapper price;

    public PriceChangeEvent(Object source, BigDecimalWrapper price) {
        super(source);
        this.price = price;
    }
}
