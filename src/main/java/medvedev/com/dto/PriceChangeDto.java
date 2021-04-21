package medvedev.com.dto;

import lombok.Data;
import medvedev.com.entity.PriceChangeEntity;
import medvedev.com.enums.HavePriceChangeState;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.wrapper.BigDecimalWrapper;

@Data
public class PriceChangeDto {

    private BigDecimalWrapper oldPrice;
    private BigDecimalWrapper newPrice;
    private PriceChangeState state;
    private HavePriceChangeState havePriceChangeState;

    public static PriceChangeDto from(PriceChangeEntity entity) {
        PriceChangeDto dto = new PriceChangeDto();
        dto.setNewPrice(entity.getNewPriceDecimal());
        dto.setOldPrice(entity.getOldPriceDecimal());
        dto.setState(entity.getState());
        dto.setHavePriceChangeState(entity.getHaveChanges());
        return dto;
    }
}
