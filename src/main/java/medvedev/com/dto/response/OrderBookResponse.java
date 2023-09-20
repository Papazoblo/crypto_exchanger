package medvedev.com.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderBookResponse {

    public static final int PRICE_INDEX = 0;
    public static final int QUANTITY_INDEX = 1;

    //покупки
    private List<String[]> bids = new ArrayList<>();
    //продажи
    private List<String[]> asks = new ArrayList<>();

    public String getBuyPriceAt(int index) {
        return asks.get(index)[PRICE_INDEX];
    }

    public String getSellPriceAt(int index) {
        return bids.get(index)[PRICE_INDEX];
    }

    public String getBuyQuantityAt(int index) {
        return asks.get(index)[QUANTITY_INDEX];
    }

    public String getSellQuantityAt(int index) {
        return asks.get(index)[QUANTITY_INDEX];
    }
}
