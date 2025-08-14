package medvedev.com.service.strategy;

import lombok.extern.slf4j.Slf4j;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CandleSplitStrategy extends BaseStrategy {

    public CandleSplitStrategy(TelegramPollingService telegramPollingService) {
        super(telegramPollingService);
    }

    @Override
    public boolean isFound(List<PriceHistoryBlockEntity> blockList) {

        PriceHistoryBlockEntity c1 = blockList.get(4);
        PriceHistoryBlockEntity c2 = blockList.get(3);
        PriceHistoryBlockEntity c3 = blockList.get(2);
        PriceHistoryBlockEntity c4 = blockList.get(1);
        PriceHistoryBlockEntity c5 = blockList.get(0);

        if (isBlockWhite(c1)
                && isBlockWhite(c2)
                && isBlockWhite(c3)
                && isBlockBlack(c4)
                && isBlockWhite(c5)
                && BigDecimalWrapper.of(c5.getOpen()).isGreaterThenOrEqual(BigDecimalWrapper.of(c4.getOpen()))) {

            sendNotif("CANDLE SPLIT founded. Last " + c5.getId());
            return true;
        }
        return false;
    }
}
