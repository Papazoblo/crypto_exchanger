package medvedev.com.dto.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "binance")
public class BinanceProperty {

    private String url;
    private String url1;
    private String key;
    private String secretKey;
    private String symbol;
    private Long rectWindow;
}
