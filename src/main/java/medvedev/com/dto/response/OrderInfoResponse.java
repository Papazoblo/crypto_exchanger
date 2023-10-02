package medvedev.com.dto.response;

import lombok.Data;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import medvedev.com.enums.OrderType;

@Data
public class OrderInfoResponse {

    private String symbol;
    private Long orderId;
    private String price;
    private String origQty;
    private String executedQty;
    private OrderStatus status;
    private OrderType type;
    private OrderSide side;
    private String icebergQty;
    private Long time;
    private Long updateTime;
}
