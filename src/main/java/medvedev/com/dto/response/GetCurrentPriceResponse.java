package medvedev.com.dto.response;

import lombok.Data;

@Data
public class GetCurrentPriceResponse {

    private String symbol;
    private String price;
}
