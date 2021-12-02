package medvedev.com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import medvedev.com.entity.PriceHistoryEntity;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PriceHistoryDto {

    private LocalDateTime date;
    private BigDecimalWrapper price;
    private PriceChangeState changeState;

    public static PriceHistoryDto of(PriceHistoryEntity entity) {
        return new PriceHistoryDto(
                entity.getDate(),
                entity.getPrice(),
                entity.getChangeState()
        );
    }
}
