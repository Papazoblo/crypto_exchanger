package medvedev.com.dto.event;


import lombok.Data;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.context.ApplicationEvent;

@Data
public class CreateBuyOrderEvent extends ApplicationEvent {

    private final PriceHistoryBlockEntity lastBlock;
    private final BigDecimalWrapper priceToBuy;
    private final String priceToSell;

    public CreateBuyOrderEvent(Object source,
                               PriceHistoryBlockEntity lastBlock,
                               BigDecimalWrapper priceToBuy,
                               String priceToSell) {
        super(source);
        this.lastBlock = lastBlock;
        this.priceToBuy = priceToBuy;
        this.priceToSell = priceToSell;
    }
}
