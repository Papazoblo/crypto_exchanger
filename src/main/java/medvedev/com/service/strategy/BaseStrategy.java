package medvedev.com.service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Log4j2
public abstract class BaseStrategy implements AnalyzerStrategy {

    private final TelegramPollingService telegramPollingService;

    /*private void checkToSell(PriceHistoryBlockEntity first,
                             PriceHistoryBlockEntity middle,
                             PriceHistoryBlockEntity last) {

        boolean isLastBodyGreaterThenFirst = isBodyGreaterThen(last, first);
        boolean isLastGreaterThenMiddle = isBodyGreaterThen(last, middle);
        boolean isMiddleBullish = isBlockBullish(middle, first);
        boolean isLess = BigDecimalWrapper.of(last.getClose()).isLessThen(BigDecimalWrapper.of(middle.getClose()));
        boolean isFirstWhite = BigDecimalWrapper.of(first.getOpen()).isLessThen(BigDecimalWrapper.of(first.getClose()));
        boolean isMiddleWhite = BigDecimalWrapper.of(middle.getOpen()).isLessThen(BigDecimalWrapper.of(middle.getClose()));
        boolean isLastBlack = BigDecimalWrapper.of(last.getClose()).isLessThen(BigDecimalWrapper.of(last.getOpen()));

        if (isLastBodyGreaterThenFirst
                && isLastGreaterThenMiddle
                && isMiddleBullish
                && isLess
                && isLastBlack
                && isFirstWhite
                && isMiddleWhite) {

            candleAnalyzeLogService.save(first, middle, last, priceProcessingService.getCurrentPrice().toString(), OrderSide.SELL);
        }
    }*/

    /**
     * тело блока fist больше, чем last
     */
    protected boolean isBodyGreaterThen(PriceHistoryBlockEntity first, PriceHistoryBlockEntity last) {
        return getBodySize(first).isGreaterThen(getBodySize(last));
    }

    /**
     * Размер свечи
     *
     * @return
     */
    protected BigDecimalWrapper getBodySize(PriceHistoryBlockEntity block) {
        return getDifference(block.getOpen(), block.getClose());
    }

    protected BigDecimalWrapper getDifference(String val1, String val2) {
        return BigDecimalWrapper.of(val1).subtract(new BigDecimalWrapper(val2)).abs();
    }

    /**
     * тело блока fist меньше, чем last
     */
    protected boolean isBodyLessThen(PriceHistoryBlockEntity last, PriceHistoryBlockEntity other) {
        return getBodySize(last).isLessThenOrEqual(getBodySize(other));
    }

    /**
     * Является ли блок медвежьим, спадающим
     *
     * @param current  - основной
     * @param previous - второстепенный
     */
    protected boolean isBlockBearish(PriceHistoryBlockEntity current, PriceHistoryBlockEntity previous) {
        return BigDecimalWrapper.of(current.getOpen()).isLessThen(BigDecimalWrapper.of(previous.getOpen()))
                && BigDecimalWrapper.of(current.getClose()).isLessThen(BigDecimalWrapper.of(previous.getClose()));
    }

    /**
     * Является ли блок бычьи, восходящим
     *
     * @param current
     * @param previous
     * @return
     */
    protected boolean isBlockBullish(PriceHistoryBlockEntity current, PriceHistoryBlockEntity previous) {
        return BigDecimalWrapper.of(current.getOpen()).isGreaterThen(BigDecimalWrapper.of(previous.getOpen()))
                && BigDecimalWrapper.of(current.getClose()).isGreaterThen(BigDecimalWrapper.of(previous.getClose()));
    }

    /**
     * Является ли блок белым, возрастающим
     *
     * @param block
     * @return
     */
    protected boolean isBlockWhite(PriceHistoryBlockEntity block) {
        return BigDecimalWrapper.of(block.getClose()).isGreaterThen(BigDecimalWrapper.of(block.getOpen()));
    }

    /**
     * Является ли блок черным, убывающим
     *
     * @param block
     * @return
     */
    protected boolean isBlockBlack(PriceHistoryBlockEntity block) {
        return !isBlockWhite(block);
    }

    /**
     * Закрывается ли курс закрытия текущего блока выше, чем предыдущий блок
     *
     * @param current
     * @param previous
     * @return
     */
    protected boolean isClosedAfter(PriceHistoryBlockEntity current, PriceHistoryBlockEntity previous) {
        return BigDecimalWrapper.of(current.getClose()).isGreaterThen(BigDecimalWrapper.of(previous.getClose()));
    }

    /**
     * Является ли тело блока очень малым
     *
     * @param block
     * @return
     */
    protected boolean isBlockBodyIsLittle(PriceHistoryBlockEntity block) {
        double percent = 0.1;

        double openDouble = BigDecimalWrapper.of(block.getOpen()).doubleValue();
        double closeDouble = BigDecimalWrapper.of(block.getClose()).doubleValue();
        double resultPercent = ((Math.abs(openDouble - closeDouble) * 100) / Math.max(openDouble, closeDouble));
        log.info("isBlockBodyIsLittle [{} - {} = {}, {}] < {} = {}", openDouble, closeDouble, (Math.abs(openDouble - closeDouble)), resultPercent, resultPercent < percent, percent);
        return resultPercent < percent;
    }

    protected boolean isBigDownShadow(PriceHistoryBlockEntity block) {
        BigDecimalWrapper bodySize = getBodySize(block);
        BigDecimalWrapper downShadowSize = getDifference(block.getOpen(), block.getMin().toString());

        return (downShadowSize.doubleValue() / bodySize.doubleValue()) > 2.5;
    }

    protected boolean isLittleUpShadow(PriceHistoryBlockEntity block) {
        BigDecimalWrapper bodySize = getBodySize(block);
        BigDecimalWrapper upShadowSize = getDifference(block.getClose(), block.getMax().toString());

        return (upShadowSize.doubleValue() / bodySize.doubleValue()) < 0.5;
    }

    protected void sendNotif(String message) {
        telegramPollingService.sendMessage(message);
    }
}
