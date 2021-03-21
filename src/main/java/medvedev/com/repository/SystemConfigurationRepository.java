package medvedev.com.repository;

import medvedev.com.entity.SystemConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfigurationEntity, Short> {

    Optional<SystemConfigurationEntity> findByName(String name);
}
