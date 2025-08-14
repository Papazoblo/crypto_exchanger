package medvedev.com.entity;

import lombok.Data;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.enums.ExchangeCancelType;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import medvedev.com.wrapper.BigDecimalWrapper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;

@Table(schema = "cr_schema", name = "exchange_history")
@Entity
@Data
public class ExchangeHistoryEntity {

    @Id
    @SequenceGenerator(schema = "cr_schema",
            sequenceName = "exchange_history_id_seq",
            name = "exchange_history_id_seq_GEN",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exchange_history_id_seq_GEN")
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

    @Column(name = "price_to_sell")
    private String priceToSell;

    @Column(name = "price")
    private String price;

    @Column(name = "stop_price")
    private String stopPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "id_prev_exchange")
    private ExchangeHistoryEntity prevExchange;

    @Column(name = "history_price_block_id")
    private Long historyPriceBlockId;

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
        entity.setOperationType(order.getSide());
        entity.setOrderId(order.getOrderId());
        entity.setOrderStatus(order.getStatus());
        return entity;
    }

}
