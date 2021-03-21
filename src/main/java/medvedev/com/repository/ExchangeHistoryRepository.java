package medvedev.com.repository;

import com.binance.api.client.domain.OrderSide;
import medvedev.com.entity.ExchangeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistoryEntity, Long> {

    List<ExchangeHistoryEntity> findAllByTypeAndIdPrevExchangeIsNotNull(OrderSide type);

    List<ExchangeHistoryEntity> findAllByDateTimeGreaterThanAndIdPrevExchangeIsNotNull(LocalDateTime startDate);
}
