package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Table(schema = "cr_schema", name = "price_history")
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
