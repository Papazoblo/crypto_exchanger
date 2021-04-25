package medvedev.com.entity;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Table(name = "exchange_history")
@Entity
@Data
public class ExchangeHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private OrderSide operationType;

    @Column(name = "datetime")
    private LocalDateTime dateTime;

    @Column(name = "initial_amount")
    private String initialAmount;

    @Column(name = "final_amount")
    private String finalAmount;

    @Column(name = "price")
    private String price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "id_prev_exchange")
    private Long idPrevExchange;

    public static ExchangeHistoryEntity from(NewOrderResponse response) {
        ExchangeHistoryEntity entity = new ExchangeHistoryEntity();
        entity.setDateTime(new Timestamp(response.getTransactTime()).toLocalDateTime());
        entity.setInitialAmount(response.getOrigQty());
        entity.setFinalAmount(response.getExecutedQty());
        entity.setPrice(response.getPrice());
        entity.setOperationType(response.getSide());
        entity.setOrderId(response.getOrderId());
        entity.setOrderStatus(response.getStatus());
        return entity;
    }
}
