package medvedev.com.service;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.Order;
import lombok.RequiredArgsConstructor;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.repository.ExchangeHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.binance.api.client.domain.OrderStatus.NEW;
import static com.binance.api.client.domain.OrderStatus.PARTIALLY_FILLED;

@Service
@RequiredArgsConstructor
public class ExchangeHistoryService {

    private final ExchangeHistoryRepository exchangeHistoryRepository;

    public void alterStatusById(Long id, OrderStatus status) {
        exchangeHistoryRepository.alterStatusById(status, id);
    }

    public ExchangeHistoryDto save(ExchangeHistoryEntity entity) {
        return ExchangeHistoryDto.from(exchangeHistoryRepository.save(entity));
    }

    public boolean saveIfNotExist(Order order) {
        if (exchangeHistoryRepository.existsByOrderId(order.getOrderId())) {
            return false;
        } else {
            exchangeHistoryRepository.save(ExchangeHistoryEntity.from(order));
            return true;
        }
    }

    public void closingOpenedExchangeById(List<ExchangeHistoryDto> openedExchange, ExchangeHistoryDto lastExchange) {
        exchangeHistoryRepository.closingOpenedExchangeById(openedExchange.stream()
                        .mapToLong(ExchangeHistoryDto::getId)
                        .boxed()
                        .collect(Collectors.toList()),
                lastExchange.getId());
    }

    public ExchangeHistoryDto getNewExchange() {
        return exchangeHistoryRepository.findFirstByOrderStatusIn(Arrays.asList(NEW, PARTIALLY_FILLED))
                .map(ExchangeHistoryDto::from)
                .orElseThrow(() -> new EntityNotFoundException("New exchange history"));
    }

    /**
     * Получение обменов ФИАТ => КРИПТА, в статусе ВЫПОЛНЕН, без
     * проставленного idPrev (т.е. цикл обмена не завершился)
     */
    public List<ExchangeHistoryDto> getAllOpenExchange() {
        List<ExchangeHistoryEntity> openedBuyExchange = exchangeHistoryRepository.findOpenedBuyExchange(
                OrderSide.BUY, OrderStatus.FILLED);
        if (!openedBuyExchange.isEmpty()) {
            return toDto(Collections.singletonList(openedBuyExchange.get(0)));
        }
        return Collections.emptyList();
    }

    /**
     * Из списка незавершенных обменов выбираем те, у которых
     * курс обмена МЕНЬШЕ ТЕКУЩЕГО курса
     */
    public List<ExchangeHistoryDto> getOpenProfitableExchange(BigDecimal lastPrice) {
        return getAllOpenExchange().stream()
                .filter(record -> record.getPrice().isLessThen(lastPrice))
                .collect(Collectors.toList());
    }

    public boolean isExistExchangeSell() {
        return exchangeHistoryRepository.existsByOperationTypeAndOrderStatus(OrderSide.SELL, OrderStatus.FILLED);
    }

    public Optional<ExchangeHistoryDto> getLastExchange() {

        return exchangeHistoryRepository.findTopByOrderStatusOrderByIdDesc(OrderStatus.FILLED)
                .map(ExchangeHistoryDto::from);
    }

    public ExchangeHistoryDto findLastSellExchange() {
        return exchangeHistoryRepository.findFirstByOperationTypeAndOrderStatusOrderByDateTimeDesc(OrderSide.SELL,
                OrderStatus.FILLED)
                .map(ExchangeHistoryDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Sell exchange not found"));
    }

    private static List<ExchangeHistoryDto> toDto(List<ExchangeHistoryEntity> entities) {
        return entities.stream()
                .map(ExchangeHistoryDto::from)
                .collect(Collectors.toList());
    }
}
