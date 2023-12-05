package medvedev.com.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "asset_balance")
@Entity
@Data
public class AssetBalanceEntity {

    @Id
    @SequenceGenerator(name = "asset_balance_seq_id_GENERATOR", sequenceName = "asset_balance_seq_id",
            allocationSize = 1)
    @GeneratedValue(generator = "asset_balance_seq_id_GENERATOR", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "usdt_info")
    private String usdt;

    @Column(name = "eth_info")
    private String eth;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
    }
}
