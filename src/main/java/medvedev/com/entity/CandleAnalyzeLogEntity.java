package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.OrderSide;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(schema = "cr_schema", name = "candle_analyze_log")
@Entity
@Data
public class CandleAnalyzeLogEntity {

    @Id
    @SequenceGenerator(schema = "cr_schema",
            name = "candle_analyze_log_seq_id_GENERATOR",
            sequenceName = "candle_analyze_log_seq_id",
            allocationSize = 1)
    @GeneratedValue(generator = "candle_analyze_log_seq_id_GENERATOR", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "first_block_id")
    private Long firstBlockId;

    @Column(name = "middle_block_id")
    private Long middleBlockId;

    @Column(name = "last_block_id")
    private Long lastBlockId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private OrderSide type;

    @Column(name = "price")
    private String price;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
    }
}
