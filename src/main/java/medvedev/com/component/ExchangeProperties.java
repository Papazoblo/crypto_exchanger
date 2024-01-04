package medvedev.com.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "exchange")
@Data
public class ExchangeProperties {

    private double sellPriceIncrement;
    private int minutesCountLiveSellOrder;
    private int boundUpdatePrice;
    private double priceDifference;
    private double doublePriceDifference;
    private int timeBetweenExchange;
    private int timeLiveBuyExchange;
    private int timeLiveSellExchange;
    private int timeLongToBackChangePrice;
}
