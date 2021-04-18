package medvedev.com.dto;

import lombok.Data;
import medvedev.com.entity.PriceChangeEntity;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

@Data
public class PriceChangeDto {

    private BigDecimalWrapper oldPrice;
    private BigDecimalWrapper newPrice;
    private PriceChangeState state;

    public static PriceChangeDto from(PriceChangeEntity entity) {
        PriceChangeDto dto = new PriceChangeDto();
        dto.setNewPrice(entity.getNewPriceDecimal());
        dto.setOldPrice(entity.getOldPriceDecimal());
        dto.setState(entity.getState());
        return dto;
    }
}
