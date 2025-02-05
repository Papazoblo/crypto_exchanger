package medvedev.com.repository;

import medvedev.com.entity.CandleAnalyzeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandleAnalyzeLogRepository extends JpaRepository<CandleAnalyzeLogEntity, Long> {

    Optional<CandleAnalyzeLogEntity> findFirstByIdIsNotNullOrderByIdDesc();
}
