package medvedev.com.service.strategy;

import lombok.extern.slf4j.Slf4j;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ThreeCandleStrategy extends BaseStrategy {
    public ThreeCandleStrategy(TelegramPollingService telegramPollingService) {
        super(telegramPollingService);
    }

    @Override
    public boolean isFound(List<PriceHistoryBlockEntity> blockList) {

        PriceHistoryBlockEntity c1 = blockList.get(4);
        PriceHistoryBlockEntity c2 = blockList.get(3);
        PriceHistoryBlockEntity c3 = blockList.get(2);
        PriceHistoryBlockEntity c4 = blockList.get(1);
        PriceHistoryBlockEntity c5 = blockList.get(0);

        if (isBlockWhite(c5)
                && isBlockWhite(c1)
                && isBlockBlack(c2)
                && isBlockBlack(c3)
                && isBlockBlack(c4)
                && BigDecimalWrapper.of(c2.getOpen()).isLessThen(BigDecimalWrapper.of(c1.getClose()))
                && BigDecimalWrapper.of(c3.getOpen()).isLessThen(BigDecimalWrapper.of(c1.getClose()))
                && BigDecimalWrapper.of(c4.getOpen()).isLessThen(BigDecimalWrapper.of(c1.getClose()))
                && BigDecimalWrapper.of(c2.getOpen()).isLessThen(BigDecimalWrapper.of(c5.getClose()))
                && BigDecimalWrapper.of(c3.getOpen()).isLessThen(BigDecimalWrapper.of(c5.getClose()))
                && BigDecimalWrapper.of(c4.getOpen()).isLessThen(BigDecimalWrapper.of(c5.getClose()))
                && BigDecimalWrapper.of(c2.getClose()).isGreaterThen(BigDecimalWrapper.of(c1.getOpen()))
                && BigDecimalWrapper.of(c3.getClose()).isGreaterThen(BigDecimalWrapper.of(c1.getOpen()))
                && BigDecimalWrapper.of(c4.getClose()).isGreaterThen(BigDecimalWrapper.of(c1.getOpen()))
                && BigDecimalWrapper.of(c2.getClose()).isGreaterThen(BigDecimalWrapper.of(c5.getOpen()))
                && BigDecimalWrapper.of(c3.getClose()).isGreaterThen(BigDecimalWrapper.of(c5.getOpen()))
                && BigDecimalWrapper.of(c4.getClose()).isGreaterThen(BigDecimalWrapper.of(c5.getOpen()))) {

            sendNotif("THREE CANDLE founded. Last " + c5.getId());
            return true;
        }
        return false;
    }
}
