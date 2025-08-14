package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.event.CandleCloseEvent;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.BlockTimeType;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.enums.SystemState;
import static medvedev.com.enums.SystemState.LAUNCHED;
import static medvedev.com.service.ExchangeService.EXCHANGE_MESSAGE_PATTERN;
import medvedev.com.service.strategy.AnalyzerStrategy;
import medvedev.com.service.telegram.TelegramPollingService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class CandleAnalyzerService {

    private final PriceHistoryBlockService priceHistoryBlockService;
    private final CandleAnalyzeLogService candleAnalyzeLogService;
    private final ExchangeService exchangeService;
    private final SystemConfigurationService systemConfigurationService;
    private final List<AnalyzerStrategy> analyzerStrategy;
    private final BinanceClientService binanceClientService;
    private final ExchangeHistoryService exchangeHistoryService;
    private final TelegramPollingService telegramPollingService;

    @EventListener
    public void analyzingCandle(CandleCloseEvent event) {

        int limit = 5;

        if (event.getTimeType().isExclude()) {
            return;
        }

        List<PriceHistoryBlockEntity> lastBlockList = priceHistoryBlockService.getLastLimitBlock(event.getTimeType(), limit);
        if (lastBlockList.size() == limit) {
//            Optional<CandleAnalyzeLogEntity> optional = candleAnalyzeLogService.getLast();
//            if (optional.isPresent()) {
//                if (optional.get().getType() == OrderSide.BUY) {
//                    checkToSell(lastBlockList.get(2), lastBlockList.get(1), lastBlockList.get(0));
//                } else {
//                    checkToBuy(lastBlockList.get(2), lastBlockList.get(1), lastBlockList.get(0));
//                }
//            } else {
            //checkToSell(lastBlockList.get(2), lastBlockList.get(1), lastBlockList.get(0));

            log.info("*** START ANALYZE {} {}", lastBlockList.get(0).getId(), event.getTimeType());
            checkToBuy(lastBlockList);
//            }
        }
    }

    public void test(Long id, BlockTimeType timeType) {
        int limit = 5;

        List<PriceHistoryBlockEntity> lastBlockList = priceHistoryBlockService.getLastLimitBlock(id, timeType, limit);
        if (lastBlockList.size() == limit) {
            checkToBuy(lastBlockList);
        }
    }

    public void createSell() {
        exchangeHistoryService.findFirst(OrderSide.BUY, OrderStatus.FILLED).ifPresent(exchangeHistoryEntity -> {
            exchangeService.createSellOrder(exchangeHistoryEntity, null);
        });
    }

    public void createMessage() {
        exchangeHistoryService.findFirst(OrderSide.BUY, OrderStatus.FILLED).ifPresent(item ->
                telegramPollingService.sendMessage(String.format(EXCHANGE_MESSAGE_PATTERN, item.getOperationType().name(),
                        item.getPrice(),
                        item.getInitialAmount(),
                        item.getInitialAmount().multiply(item.getPrice()).setScale(4, RoundingMode.HALF_EVEN),
                        item.getPriceToSell())));

    }

    /**
     * Триггер Создания ордера на покупку
     */
    private void checkToBuy(List<PriceHistoryBlockEntity> blockList) {

        if (analyzerStrategy.stream()
                .parallel()
                .anyMatch(strategy -> strategy.isFound(blockList))) {

            BigDecimalWrapper currentPrice = binanceClientService.getCurrentPrice();
            candleAnalyzeLogService.save(blockList.get(2), blockList.get(1), blockList.get(0), currentPrice.toString(), OrderSide.BUY);

            SystemState state = SystemState.valueOf(systemConfigurationService.findByName(SystemConfiguration.SYSTEM_STATE));
            if (state == LAUNCHED) {

                exchangeService.createBuyOrder(blockList.get(0),
                        currentPrice,
                        currentPrice.multiply(BigDecimalWrapper.valueOf(1.004)).setScale(2, RoundingMode.HALF_UP).toString());
            }
        }
    }
}
