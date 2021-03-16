package medvedev.com.entity;

import com.binance.api.client.domain.OrderSide;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "exchange_history")
@Entity
@Data
public class ExchangeHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type")
    private OrderSide type;

    @Column(name = "datetime")
    private LocalDateTime dateTime;

    @Column(name = "initial_amount")
    private String initialAmount;

    @Column(name = "final_amount")
    private String finalAmount;

    @Column(name = "rate")
    private String rate;

    @Column(name = "comission")
    private String commission;

    @Column(name = "id_prev_exchange")
    private Long idPrevExchange;
}
