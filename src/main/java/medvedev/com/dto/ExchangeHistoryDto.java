package medvedev.com.dto;

import com.binance.api.client.domain.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.wrapper.BigDecimalWrapper;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ExchangeHistoryDto {

    private Long id;
    private OrderSide exchangeType;
    private LocalDateTime dateTime;
    private BigDecimalWrapper initialAmount;
    private BigDecimalWrapper finalAmount;
    private BigDecimalWrapper commission;
    private BigDecimalWrapper rate;
    private Long idPrevExchange;

    public static ExchangeHistoryDto from(ExchangeHistoryEntity entity) {
        return new ExchangeHistoryDto(
                entity.getId(),
                entity.getType(),
                entity.getDateTime(),
                new BigDecimalWrapper(entity.getInitialAmount()),
                new BigDecimalWrapper(entity.getFinalAmount()),
                new BigDecimalWrapper(entity.getCommission()),
                new BigDecimalWrapper(entity.getRate()),
                entity.getIdPrevExchange()
        );
    }
}
