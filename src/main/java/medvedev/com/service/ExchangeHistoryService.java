package medvedev.com.service;


import lombok.RequiredArgsConstructor;
import medvedev.com.dto.response.OrderInfoResponse;
import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.entity.PriceHistoryBlockEntity;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import medvedev.com.repository.ExchangeHistoryRepository;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeHistoryService {

    private final ExchangeHistoryRepository exchangeHistoryRepository;

    public ExchangeHistoryEntity save(ExchangeHistoryEntity entity) {
        return exchangeHistoryRepository.save(entity);
    }

    public List<ExchangeHistoryEntity> findAllByStatus(List<OrderStatus> statusList) {
        return exchangeHistoryRepository.findAllByOrderStatusIn(statusList);
    }

    public void saveIfNotExist(ExchangeHistoryEntity lastExchange,
                               OrderInfoResponse order,
                               PriceHistoryBlockEntity block,
                               String priceToSell) {
        if (!exchangeHistoryRepository.existsByOrderId(order.getOrderId())) {
            ExchangeHistoryEntity newExchangeItem = ExchangeHistoryEntity.from(order);
            newExchangeItem.setPriceToSell(priceToSell);
            if (order.getSide() == OrderSide.BUY) {
                newExchangeItem.setHistoryPriceBlockId(block.getId());
                newExchangeItem.setStopPrice((newExchangeItem.getPrice().multiply(BigDecimalWrapper.valueOf(0.997)).setScale(2, RoundingMode.HALF_UP).toString()));
            }
            newExchangeItem.setPrevExchange(lastExchange);
            exchangeHistoryRepository.save(newExchangeItem);
        }
    }

    public Optional<ExchangeHistoryEntity> findFirst(OrderSide side, OrderStatus status) {
        return exchangeHistoryRepository.findFirstByOperationTypeAndOrderStatusOrderByCreateDateDesc(side, status);
    }

    public Optional<ExchangeHistoryEntity> findLast() {
        return exchangeHistoryRepository.findLastOrder();
    }
}
