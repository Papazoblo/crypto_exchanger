package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "price_history")
@Entity
@Data
public class PriceHistoryEntity {

    @Id
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "price")
    private String price;

    @Column(name = "change_state")
    @Enumerated(EnumType.STRING)
    private PriceChangeState changeState;

    public BigDecimalWrapper getPrice() {
        return new BigDecimalWrapper(price);
    }
}
