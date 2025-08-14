package medvedev.com.service.strategy;

import lombok.extern.slf4j.Slf4j;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PushStrategy extends BaseStrategy {

    public PushStrategy(TelegramPollingService telegramPollingService) {
        super(telegramPollingService);
    }

    @Override
    public boolean isFound(List<PriceHistoryBlockEntity> blockList) {

        PriceHistoryBlockEntity first = blockList.get(2);
        PriceHistoryBlockEntity middle = blockList.get(1);
        PriceHistoryBlockEntity last = blockList.get(0);

        if (isBlockBlack(first)
                && isBlockWhite(middle)
                && isBlockWhite(last)
                && BigDecimalWrapper.of(middle.getOpen()).isLessThen(first.getMin())
                && BigDecimalWrapper.of(middle.getClose()).isGreaterThenOrEqual(BigDecimalWrapper.of(first.getClose()))) {

            sendNotif("PUSH founded. Last " + last.getId());
            return true;
        }
        return false;
    }
}
