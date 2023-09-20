package medvedev.com.component;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import medvedev.com.dto.property.BinanceProperty;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static medvedev.com.utils.SigningUtils.createSign;

@RequiredArgsConstructor
public class BinanceApiInterceptor implements RequestInterceptor {

    private final BinanceProperty properties;

    @Override
    public void apply(RequestTemplate template) {
        if (!template.url().contains("/api/v3/time")
                && !template.url().contains("/api/v3/ticker/price")
                && !template.url().contains("/api/v3/trades")
                && !template.url().contains("/api/v3/depth")) {
            String params = template.queries().entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), new ArrayList<>(entry.getValue()).get(0)))
                    .collect(Collectors.joining("&"));
            template.request().requestTemplate().query("signature", createSign(params, properties.getSecretKey()));
            template.header("X-MBX-APIKEY", properties.getKey());
        }
    }
}
