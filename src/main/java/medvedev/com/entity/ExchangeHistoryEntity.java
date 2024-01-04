package medvedev.com.entity;

import lombok.Data;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.enums.ExchangeCancelType;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

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
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "initial_amount")
    private String initialAmount;

    @Column(name = "final_amount")
    private String finalAmount;

    @Column(name = "min_price_exchange")
    private String minPriceExchange;

    @Column(name = "increment_step")
    private Integer incrementStep;

    @Column(name = "price")
    private String price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "id_prev_exchange")
    private ExchangeHistoryEntity prevExchange;

    @Column(name = "cancel_type")
    @Enumerated(EnumType.STRING)
    private ExchangeCancelType cancelType;

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
        this.updateDate = this.createDate;
    }

    public BigDecimalWrapper getFinalAmount() {
        return Optional.ofNullable(finalAmount).map(BigDecimalWrapper::new).orElse(null);
    }

    public BigDecimalWrapper getInitialAmount() {
        return Optional.ofNullable(initialAmount).map(BigDecimalWrapper::new).orElse(null);
    }

    public BigDecimalWrapper getMinPriceExchange() {
        return Optional.ofNullable(minPriceExchange).map(BigDecimalWrapper::new).orElse(null);
    }

    public BigDecimalWrapper getPrice() {
        return Optional.ofNullable(price).map(BigDecimalWrapper::new).orElse(null);
    }

    @PreUpdate
    public void preUpdate() {
        this.updateDate = LocalDateTime.now();
    }

    public static ExchangeHistoryEntity from(OrderInfoResponse order) {
        ExchangeHistoryEntity entity = new ExchangeHistoryEntity();
        entity.setInitialAmount(order.getOrigQty());
        entity.setFinalAmount(order.getExecutedQty());
        entity.setPrice(order.getPrice());
        if (entity.getMinPriceExchange() == null) {
            entity.setMinPriceExchange(entity.getPrice().toString());
        }
        entity.setOperationType(order.getSide());
        entity.setOrderId(order.getOrderId());
        entity.setOrderStatus(order.getStatus());
        entity.setIncrementStep(0);
        return entity;
    }

}
