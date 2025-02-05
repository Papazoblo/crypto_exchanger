package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.HavePriceChangeState;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.*;

import static medvedev.com.enums.HavePriceChangeState.WITHOUT_CHANGES;
import static medvedev.com.enums.PriceChangeState.DECREASED;
import static medvedev.com.enums.PriceChangeState.INCREASED;

@Table(schema = "cr_schema", name = "price_changes")
@Entity
@Data
public class PriceChangeEntity {

    @Id
    @SequenceGenerator(schema = "cr_schema",
            sequenceName = "price_changes_id_seq",
            name = "price_changes_id_seq_GEN",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_changes_id_seq_GEN")
    private Long id;

    @Column(name = "old")
    private String oldPrice;

    @Column(name = "new")
    private String newPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private PriceChangeState state;

    @Enumerated(EnumType.STRING)
    @Column(name = "have_changes")
    private HavePriceChangeState haveChanges;

    public void setState(PriceChangeState newState) {
        if (state == INCREASED && newState == DECREASED || state == DECREASED && newState == INCREASED) {
            haveChanges = HavePriceChangeState.WITH_CHANGES;
        } else if (newState == state) {
            haveChanges = WITHOUT_CHANGES;
        }
        state = newState;
    }

    public BigDecimalWrapper getOldPriceDecimal() {
        return new BigDecimalWrapper(oldPrice);
    }

    public BigDecimalWrapper getNewPriceDecimal() {
        return new BigDecimalWrapper(newPrice);
    }
}
