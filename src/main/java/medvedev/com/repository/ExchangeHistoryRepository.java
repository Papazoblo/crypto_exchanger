package medvedev.com.repository;

import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistoryEntity, Long> {

    Optional<ExchangeHistoryEntity> findFirstByOperationTypeAndOrderStatusOrderByCreateDateDesc(OrderSide orderSide,
                                                                                                OrderStatus orderStatus);

    @Query(value = "select eh.* " +
            "from cr_schema.exchange_history eh " +
            "order by eh.id desc " +
            "limit 1", nativeQuery = true)
    Optional<ExchangeHistoryEntity> findLastOrder();

    List<ExchangeHistoryEntity> findAllByOrderStatusIn(List<OrderStatus> statusList);

    boolean existsByOrderId(Long idOrder);
}
