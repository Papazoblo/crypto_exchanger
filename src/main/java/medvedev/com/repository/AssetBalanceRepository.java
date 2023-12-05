package medvedev.com.repository;

import medvedev.com.entity.AssetBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetBalanceRepository extends JpaRepository<AssetBalanceEntity, Long> {

}
