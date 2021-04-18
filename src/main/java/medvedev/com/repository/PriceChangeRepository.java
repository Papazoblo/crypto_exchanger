package medvedev.com.repository;

import medvedev.com.entity.PriceChangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PriceChangeRepository extends JpaRepository<PriceChangeEntity, Short> {

    @Query(value = "SELECT pc FROM PriceChangeEntity pc WHERE pc.id IS NOT NULL")
    Optional<PriceChangeEntity> findFirstById();
}
