package medvedev.com.service;


import lombok.RequiredArgsConstructor;
import medvedev.com.dto.ExchangeHistoryDto;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.repository.ExchangeHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static medvedev.com.enums.OrderStatus.NEW;
import static medvedev.com.enums.OrderStatus.PARTIALLY_FILLED;

@Service
@RequiredArgsConstructor
public class ExchangeHistoryService {

    private final ExchangeHistoryRepository exchangeHistoryRepository;

    public void alterStatusById(Long id, OrderStatus status) {
        exchangeHistoryRepository.alterStatusById(status, id);
    }

    public Optional<ExchangeHistoryEntity> findLastOrder() {
        return exchangeHistoryRepository.findLastOrder();
    }

    public ExchangeHistoryDto save(ExchangeHistoryEntity entity) {
        return ExchangeHistoryDto.from(exchangeHistoryRepository.save(entity));
    }

    public boolean saveIfNotExist(OrderInfoResponse order) {
        if (exchangeHistoryRepository.existsByOrderId(order.getOrderId())) {
            return false;
        } else {
            exchangeHistoryRepository.save(ExchangeHistoryEntity.from(order));
            return true;
        }
    }

    public void closingOpenedExchangeById(Long id) {
        exchangeHistoryRepository.closingOpenedExchangeById(id);
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
        return exchangeHistoryRepository.findFirstByOperationTypeAndOrderStatusOrderByCreateDateDesc(OrderSide.SELL,
                        OrderStatus.FILLED)
                .map(ExchangeHistoryDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Sell exchange not found"));
    }

    public ExchangeHistoryDto findLastBuyFilledExchange() {
        return exchangeHistoryRepository.findFirstByOperationTypeAndOrderStatusOrderByCreateDateDesc(OrderSide.BUY,
                        OrderStatus.FILLED)
                .map(ExchangeHistoryDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Buy exchange not found"));
    }

    private static List<ExchangeHistoryDto> toDto(List<ExchangeHistoryEntity> entities) {
        return entities.stream()
                .map(ExchangeHistoryDto::from)
                .collect(Collectors.toList());
    }
}
