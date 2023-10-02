package medvedev.com.entity;

import lombok.Data;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;

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

    /*public static ExchangeHistoryEntity from(NewOrderResponse response, PriceHistoryDto priceHistory) {
        ExchangeHistoryEntity entity = new ExchangeHistoryEntity();
        entity.setDateTime(new Timestamp(response.getTransactTime()).toLocalDateTime());
        entity.setInitialAmount(response.getOrigQty());
        entity.setFinalAmount(response.getExecutedQty());
        entity.setPrice(priceHistory.getPrice().toString());
        entity.setOperationType(response.getSide());
        entity.setOrderId(response.getOrderId());
        entity.setOrderStatus(response.getStatus());
        return entity;
    }*/

    public static ExchangeHistoryEntity from(OrderInfoResponse order) {
        ExchangeHistoryEntity entity = new ExchangeHistoryEntity();
        entity.setDateTime(new Timestamp(order.getTime()).toLocalDateTime());
        entity.setInitialAmount(order.getOrigQty());
        entity.setFinalAmount(order.getExecutedQty());
        entity.setPrice((new BigDecimal(order.getPrice()).subtract(BigDecimal.ONE)).toString());
        entity.setOperationType(order.getSide());
        entity.setOrderId(order.getOrderId());
        entity.setOrderStatus(order.getStatus());
        return entity;
    }
}
