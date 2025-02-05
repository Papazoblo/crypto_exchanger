package medvedev.com.repository;

import medvedev.com.entity.ExchangeHistoryEntity;
import medvedev.com.enums.OrderSide;
import medvedev.com.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistoryEntity, Long> {

    @Query(value = "select eh.* from cr_schema.exchange_history eh " +
            "order by eh.id desc limit 1", nativeQuery = true)
    Optional<ExchangeHistoryEntity> findLastOrder();

    Optional<ExchangeHistoryEntity> findTopByOrderStatusOrderByIdDesc(OrderStatus orderStatus);

    Optional<ExchangeHistoryEntity> findFirstByOperationTypeAndOrderStatusOrderByCreateDateDesc(OrderSide orderSide,
                                                                                              OrderStatus orderStatus);

    boolean existsByOrderId(Long idOrder);

    boolean existsByOperationTypeAndOrderStatus(OrderSide orderSide, OrderStatus orderStatus);

    Optional<ExchangeHistoryEntity> findFirstByOrderStatusIn(List<OrderStatus> statuses);

    Optional<ExchangeHistoryEntity> findTopByOrderStatusAndOperationTypeOrderByCreateDateDesc(OrderStatus status,
                                                                                            OrderSide type);

    @Query("SELECT ex " +
            "FROM ExchangeHistoryEntity ex " +
            "WHERE ex.prevExchange.id IS NULL " +
            "  AND ex.operationType = 'BUY' " +
            "  AND ex.orderStatus = 'FILLED' " +
            "ORDER BY ex.id DESC")
    List<ExchangeHistoryEntity> findOpenedBuyExchange(OrderSide type, OrderStatus status);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ExchangeHistoryEntity eh SET eh.orderStatus = :status WHERE eh.id = :id")
    void alterStatusById(@Param("status") OrderStatus status, @Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE cr_schema.exchange_history SET id_prev_exchange = :idPrev WHERE " +
            " id_prev_exchange IS NULL " +
            "            AND type = 'BUY' " +
            "            AND order_status = 'FILLED' ", nativeQuery = true)
    void closingOpenedExchangeById(@Param("idPrev") Long idPrev);
}
