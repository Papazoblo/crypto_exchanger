package medvedev.com.service.strategy;

import lombok.extern.slf4j.Slf4j;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.service.telegram.TelegramPollingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class HammerStrategy extends BaseStrategy {

    public HammerStrategy(TelegramPollingService telegramPollingService) {
        super(telegramPollingService);
    }

    @Override
    public boolean isFound(List<PriceHistoryBlockEntity> blockList) {

        PriceHistoryBlockEntity first = blockList.get(2);
        PriceHistoryBlockEntity middle = blockList.get(1);
        PriceHistoryBlockEntity last = blockList.get(0);

        if (isBlockWhite(middle)
                && isBigDownShadow(middle)
                && isLittleUpShadow(middle)
                && isClosedAfter(last, first)) {

            sendNotif("HAMMER founded. Last " + last.getId());
            return true;
        }
        return false;
    }
}
