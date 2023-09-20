package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.GetCurrentPriceResponse;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CheckPriceService {

    private final BinanceApiClient client;
    private final BinanceProperty properties;
    private final PriceHistoryService priceHistoryService;

    //фиксация текущего курса и обновление блока
    @Scheduled(fixedRateString = "${fixed-rate.check-price}")
    public void checkPriceNormal() {
        GetCurrentPriceResponse response = client.getCurrentPrice(properties.getSymbol());
        priceHistoryService.savePrice(BigDecimalWrapper.of(response.getPrice()));
    }
}
