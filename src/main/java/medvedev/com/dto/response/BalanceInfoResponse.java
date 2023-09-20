package medvedev.com.dto.response;

import lombok.Data;

@Data
public class BalanceInfoResponse {

    private String asset;
    private String free;
    private String locked;
    private String freeze;
    private String btcValuation;
}
