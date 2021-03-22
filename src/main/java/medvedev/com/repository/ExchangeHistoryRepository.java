package medvedev.com.repository;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import medvedev.com.entity.ExchangeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistoryEntity, Long> {

    List<ExchangeHistoryEntity> findAllByOperationTypeAndOrderStatusAndIdPrevExchangeIsNull(OrderSide type,
                                                                                            OrderStatus status);

    List<ExchangeHistoryEntity> findAllByDateTimeGreaterThanAndIdPrevExchangeIsNotNullAndOrderStatus(
            LocalDateTime startDate, OrderStatus status);
}
