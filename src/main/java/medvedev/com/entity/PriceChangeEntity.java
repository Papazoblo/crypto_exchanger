package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.*;

import static medvedev.com.enums.PriceChangeState.WITHOUT_CHANGES;

@Table(name = "price_changes")
@Entity
@Data
public class PriceChangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "old")
    private String oldPrice;

    @Column(name = "new")
    private String newPrice;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "id_state")
    private PriceChangeState state;

    public void setState(PriceChangeState newState) {
        if (state != newState) {
            state = newState;
        } else {
            state = WITHOUT_CHANGES;
        }
    }

    public BigDecimalWrapper getOldPriceDecimal() {
        return new BigDecimalWrapper(oldPrice);
    }

    public BigDecimalWrapper getNewPriceDecimal() {
        return new BigDecimalWrapper(newPrice);
    }
}
