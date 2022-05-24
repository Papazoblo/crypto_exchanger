package medvedev.com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.PriceBlockStatus;
import medvedev.com.enums.PriceChangeState;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PriceHistoryBlockDto {

    private Long id;
    private LocalDateTime dateOpen;
    private LocalDateTime dateClose;
    private PriceBlockStatus status;
    private String min;
    private String max;
    private String avg;
    private PriceChangeState avgChangeType;

    public static PriceHistoryBlockDto of(PriceHistoryBlockEntity entity) {
        return new PriceHistoryBlockDto(
                entity.getId(),
                entity.getDateOpen(),
                entity.getDateClose(),
                entity.getStatus(),
                entity.getMin().toString(),
                entity.getMax().toString(),
                entity.getAvg().toString(),
                entity.getAvgChangeType()
        );
    }
}
