package medvedev.com.repository;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import medvedev.com.entity.ExchangeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistoryEntity, Long> {

    Optional<ExchangeHistoryEntity> findTopByOrderStatusOrderByIdDesc(OrderStatus orderStatus);

    Optional<ExchangeHistoryEntity> findFirstByOperationTypeAndOrderStatusOrderByDateTimeDesc(OrderSide orderSide,
                                                                                              OrderStatus orderStatus);

    boolean existsByOrderId(Long idOrder);

    boolean existsByOperationTypeAndOrderStatus(OrderSide orderSide, OrderStatus orderStatus);

    Optional<ExchangeHistoryEntity> findFirstByOrderStatusIn(List<OrderStatus> statuses);

    Optional<ExchangeHistoryEntity> findTopByOrderStatusAndOperationTypeOrderByDateTimeDesc(OrderStatus status,
                                                                                            OrderSide type);

    List<ExchangeHistoryEntity> findAllByOperationTypeAndOrderStatusAndIdPrevExchangeIsNull(OrderSide type,
                                                                                            OrderStatus status);

    List<ExchangeHistoryEntity> findAllByDateTimeGreaterThanAndIdPrevExchangeIsNotNullAndOrderStatus(
            LocalDateTime startDate, OrderStatus status);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ExchangeHistoryEntity eh SET eh.orderStatus = :status WHERE eh.id = :id")
    void alterStatusById(@Param("status") OrderStatus status, @Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ExchangeHistoryEntity eh SET eh.idPrevExchange = :idPrev WHERE eh.id IN :ids")
    void closingOpenedExchangeById(@Param("ids") List<Long> ids, @Param("idPrev") Long idPrev);
}
