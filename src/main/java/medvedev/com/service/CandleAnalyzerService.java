package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.event.CandleCloseEvent;
import medvedev.com.entity.CandleAnalyzeLogEntity;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.OrderSide;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class CandleAnalyzerService {

    private final PriceHistoryBlockService priceHistoryBlockService;
    private final CandleAnalyzeLogService candleAnalyzeLogService;
    private final PriceProcessingService priceProcessingService;

    @EventListener
    public void analyzingCandle(CandleCloseEvent event) {

        List<PriceHistoryBlockEntity> lastBlockList = priceHistoryBlockService.getLast3Block();
        if (lastBlockList.size() == 3) {
            Optional<CandleAnalyzeLogEntity> optional = candleAnalyzeLogService.getLast();
            if (optional.isPresent()) {
                if (optional.get().getType() == OrderSide.BUY) {
                    checkToSell(lastBlockList.get(2), lastBlockList.get(1), lastBlockList.get(0));
                } else {
                    checkToBuy(lastBlockList.get(2), lastBlockList.get(1), lastBlockList.get(0));
                }
            } else {
                checkToSell(lastBlockList.get(2), lastBlockList.get(1), lastBlockList.get(0));
                checkToBuy(lastBlockList.get(2), lastBlockList.get(1), lastBlockList.get(0));
            }
        }
    }

    private void checkToBuy(PriceHistoryBlockEntity first,
                            PriceHistoryBlockEntity middle,
                            PriceHistoryBlockEntity last) {

        boolean isLastGreaterThenFirst = isBlockGreaterThen(last, first);
        boolean isLastGreaterThenMiddle = isBlockGreaterThen(last, middle);
        boolean isMiddleBearish = isMiddleBearish(middle, first);
        boolean isGreater = BigDecimalWrapper.of(last.getClose()).isGreaterThen(BigDecimalWrapper.of(middle.getClose()));
        boolean isFirstBlack = BigDecimalWrapper.of(first.getOpen()).isGreaterThen(BigDecimalWrapper.of(first.getClose()));
        boolean isMiddleBlack = BigDecimalWrapper.of(middle.getOpen()).isGreaterThen(BigDecimalWrapper.of(middle.getClose()));
        boolean isLastWhite = BigDecimalWrapper.of(last.getClose()).isGreaterThen(BigDecimalWrapper.of(last.getOpen()));

        log.info("ANALYZE\n\tisLastGreaterThenFirst: {}\n\t" +
                "isLastGreaterThenMiddle: {}\n\t" +
                "isMiddleBearish: {}\n\t" +
                "isGreater: {}\n\t" +
                "isLastWhite: {}\n\t", isLastGreaterThenFirst, isLastGreaterThenMiddle, isMiddleBearish, isGreater, isLastWhite);

        if (isLastGreaterThenFirst
                && isLastGreaterThenMiddle
                && isMiddleBearish
                && isGreater
                && isLastWhite
                && isMiddleBlack
                && isFirstBlack) {

            candleAnalyzeLogService.save(first, middle, last, priceProcessingService.getCurrentPrice().toString(), OrderSide.BUY);
        }
    }

    private void checkToSell(PriceHistoryBlockEntity first,
                             PriceHistoryBlockEntity middle,
                             PriceHistoryBlockEntity last) {

        boolean isLastGreaterThenFirst = isBlockGreaterThen(last, first);
        boolean isLastGreaterThenMiddle = isBlockGreaterThen(last, middle);
        boolean isMiddleBullish = isMiddleBullish(middle, first);
        boolean isLess = BigDecimalWrapper.of(last.getClose()).isLessThen(BigDecimalWrapper.of(middle.getClose()));
        boolean isFirstWhite = BigDecimalWrapper.of(first.getOpen()).isLessThen(BigDecimalWrapper.of(first.getClose()));
        boolean isMiddleWhite = BigDecimalWrapper.of(middle.getOpen()).isLessThen(BigDecimalWrapper.of(middle.getClose()));
        boolean isLastBlack = BigDecimalWrapper.of(last.getClose()).isLessThen(BigDecimalWrapper.of(last.getOpen()));

        log.info("ANALYZE\n\tisLastGreaterThenFirst: {}\n\t" +
                "isLastGreaterThenMiddle: {}\n\t" +
                "isMiddleBullish: {}\n\t" +
                "isGreater: {}\n\t" +
                "isBlack: {}\n\t", isLastGreaterThenFirst, isLastGreaterThenMiddle, isMiddleBullish, isLess, isLastBlack);

        if (isLastGreaterThenFirst
                && isLastGreaterThenMiddle
                && isMiddleBullish
                && isLess
                && isLastBlack
                && isFirstWhite
                && isMiddleWhite) {

            candleAnalyzeLogService.save(first, middle, last, priceProcessingService.getCurrentPrice().toString(), OrderSide.SELL);
        }
    }

    private boolean isBlockGreaterThen(PriceHistoryBlockEntity last, PriceHistoryBlockEntity other) {
        return BigDecimalWrapper.of(last.getOpen()).subtract(BigDecimalWrapper.of(last.getClose())).abs()
                .isGreaterThen(BigDecimalWrapper.of(other.getOpen()).subtract(BigDecimalWrapper.of(other.getClose())).abs());
    }

    private boolean isBlockLessThen(PriceHistoryBlockEntity last, PriceHistoryBlockEntity other) {
        return BigDecimalWrapper.of(last.getOpen()).subtract(BigDecimalWrapper.of(last.getClose())).abs()
                .isLessThenOrEqual(BigDecimalWrapper.of(other.getOpen()).subtract(BigDecimalWrapper.of(other.getClose())).abs());
    }

    private boolean isMiddleBearish(PriceHistoryBlockEntity middle, PriceHistoryBlockEntity first) {
        return BigDecimalWrapper.of(middle.getOpen()).isLessThen(BigDecimalWrapper.of(first.getOpen()))
                && BigDecimalWrapper.of(middle.getClose()).isLessThen(BigDecimalWrapper.of(first.getClose()));
    }

    private boolean isMiddleBullish(PriceHistoryBlockEntity middle, PriceHistoryBlockEntity first) {
        return BigDecimalWrapper.of(middle.getOpen()).isGreaterThen(BigDecimalWrapper.of(first.getOpen()))
                && BigDecimalWrapper.of(middle.getClose()).isGreaterThen(BigDecimalWrapper.of(first.getClose()));
    }

}
