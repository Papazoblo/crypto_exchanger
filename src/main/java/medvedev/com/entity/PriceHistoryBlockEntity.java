package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.BlockTimeType;
import medvedev.com.enums.PriceBlockStatus;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Table(schema = "cr_schema", name = "price_history_block")
@Entity
@Data
public class PriceHistoryBlockEntity {

    @Id
    @SequenceGenerator(schema = "cr_schema",
            name = "PRICE_HISTORY_BLOCK_ID_GENERATOR",
            sequenceName = "price_history_block_id_seq",
            allocationSize = 1)
    @GeneratedValue(generator = "PRICE_HISTORY_BLOCK_ID_GENERATOR", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "date_open")
    private LocalDateTime dateOpen;

    @Column(name = "date_close")
    private LocalDateTime dateClose;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PriceBlockStatus status;

    @Column(name = "min")
    private String min;

    @Column(name = "max")
    private String max;

    @Column(name = "avg")
    private String avg;

    @Column(name = "open")
    private String open;

    @Column(name = "close")
    private String close;

    @Column(name = "time_type")
    @Enumerated(EnumType.STRING)
    private BlockTimeType timeType;

    @Column(name = "avg_change_type")
    @Enumerated(EnumType.STRING)
    private PriceChangeState avgChangeType;

    public BigDecimalWrapper getMin() {
        String min = this.min.contains("Infinity") ? "0.0" : this.min;
        return new BigDecimalWrapper(min);
    }

    public BigDecimalWrapper getMax() {
        String max = this.max.contains("Infinity") ? "0.0" : this.max;
        return new BigDecimalWrapper(max);
    }

    public BigDecimalWrapper getAvg() {
        return new BigDecimalWrapper(avg);
    }

    @PrePersist
    public void prePersist() {
        dateOpen = LocalDateTime.now();
        status = PriceBlockStatus.OPEN;
        min = "0.0";
        max = "0.0";
        avg = "0.0";
    }
}
