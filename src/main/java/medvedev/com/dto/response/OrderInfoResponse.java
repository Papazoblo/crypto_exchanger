package medvedev.com.dto.response;

import lombok.Data;

@Data
public class OrderInfoResponse {

    private String symbol;
    private Long orderId;
    private String price;
    private String origQty;
    private String executedQty;
    private String status;
    private String type;
    private String side;
    private String icebergQty;
    private Long time;
    private Long updateTime;
}
