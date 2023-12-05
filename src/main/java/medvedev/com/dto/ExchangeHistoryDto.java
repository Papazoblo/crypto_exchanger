package medvedev.com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import medvedev.com.wrapper.BigDecimalWrapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor
public class ExchangeHistoryDto {

    private Long id;
    private Long orderId;
    private OrderSide orderType;
    private LocalDateTime dateTime;
    private BigDecimalWrapper initialAmount;
    private BigDecimalWrapper finalAmount;
    private BigDecimalWrapper price;
    private OrderStatus orderStatus;
    private Long idPrevExchange;

    public static ExchangeHistoryDto from(ExchangeHistoryEntity entity) {
        return new ExchangeHistoryDto(
                entity.getId(),
                entity.getOrderId(),
                entity.getOperationType(),
                entity.getCreateDate(),
                new BigDecimalWrapper(entity.getInitialAmount()),
                new BigDecimalWrapper(entity.getFinalAmount()),
                new BigDecimalWrapper(entity.getPrice()),
                entity.getOrderStatus(),
                Optional.ofNullable(entity.getPrevExchange()).map(ExchangeHistoryEntity::getId).orElse(null)
        );
    }
}
