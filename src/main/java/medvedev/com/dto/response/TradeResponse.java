package medvedev.com.dto.response;

import lombok.Data;

@Data
public class TradeResponse {
    private String price;
    private String qty;
    private Long time;
    private Boolean isBuyerMaker;
    private Boolean isBestMatch;
}
