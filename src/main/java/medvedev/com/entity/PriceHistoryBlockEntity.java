package medvedev.com.entity;

import lombok.Data;
import lombok.ToString;
import medvedev.com.enums.PriceBlockStatus;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.DoubleStream;

@Table(name = "price_history_block")
@Entity
@Data
public class PriceHistoryBlockEntity {

    @Id
    @SequenceGenerator(name = "PRICE_HISTORY_BLOCK_ID_GENERATOR", sequenceName = "price_history_block_id_seq",
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

    @OneToMany(mappedBy = "historyBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PriceHistoryEntity> historyList = new ArrayList<>();

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

    @PreUpdate
    public void preUpdate() {
        DoubleSummaryStatistics statistics = historyList.stream()
                .flatMapToDouble(price -> DoubleStream.of(price.getPrice().doubleValue()))
                .summaryStatistics();
        min = Double.toString(statistics.getMin());
        max = Double.toString(statistics.getMax());

        avgChangeType = Double.parseDouble(avg) > statistics.getAverage() ?
                PriceChangeState.DECREASED : PriceChangeState.INCREASED;
        avg = Double.toString(statistics.getAverage());
    }
}
