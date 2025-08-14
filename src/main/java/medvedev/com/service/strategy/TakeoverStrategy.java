package medvedev.com.service.strategy;

import lombok.extern.slf4j.Slf4j;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TakeoverStrategy extends BaseStrategy {

    public TakeoverStrategy(TelegramPollingService telegramPollingService) {
        super(telegramPollingService);
    }

    @Override
    public boolean isFound(List<PriceHistoryBlockEntity> blockList) {

        PriceHistoryBlockEntity first = blockList.get(2);
        PriceHistoryBlockEntity middle = blockList.get(1);
        PriceHistoryBlockEntity last = blockList.get(0);

        if (isBlockBlack(first)
                && isBlockBlack(middle)
                && isBlockWhite(last)
                && isBodyGreaterThen(middle, last)
                && isClosedAfter(middle, last)
                && BigDecimalWrapper.of(last.getClose()).isGreaterThen(BigDecimalWrapper.of(middle.getOpen()))
                && BigDecimalWrapper.of(last.getOpen()).isLessThen(BigDecimalWrapper.of(middle.getClose()))) {

            sendNotif("TAKEOVER founded. Last " + last.getId());
            return true;
        }
        return false;
    }
}
