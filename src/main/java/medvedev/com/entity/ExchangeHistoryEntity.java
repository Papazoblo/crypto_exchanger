package medvedev.com.entity;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import lombok.Data;
import medvedev.com.dto.PriceChangeDto;

import javax.persistence.*;
import java.math.BigDecimal;
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

    public static ExchangeHistoryEntity from(NewOrderResponse response, PriceChangeDto priceChange) {
        ExchangeHistoryEntity entity = new ExchangeHistoryEntity();
        entity.setDateTime(new Timestamp(response.getTransactTime()).toLocalDateTime());
        entity.setInitialAmount(response.getOrigQty());
        entity.setFinalAmount(response.getExecutedQty());
        entity.setPrice(priceChange.getNewPrice().toString());
        entity.setOperationType(response.getSide());
        entity.setOrderId(response.getOrderId());
        entity.setOrderStatus(response.getStatus());
        return entity;
    }

    public static ExchangeHistoryEntity from(Order order) {
        ExchangeHistoryEntity entity = new ExchangeHistoryEntity();
        entity.setDateTime(new Timestamp(order.getTime()).toLocalDateTime());
        entity.setInitialAmount(order.getOrigQty());
        entity.setFinalAmount(order.getExecutedQty());
        entity.setPrice((new BigDecimal(order.getPrice()).multiply(new BigDecimal("0.99"))).toString());
        entity.setOperationType(order.getSide());
        entity.setOrderId(order.getOrderId());
        entity.setOrderStatus(order.getStatus());
        return entity;
    }
}
