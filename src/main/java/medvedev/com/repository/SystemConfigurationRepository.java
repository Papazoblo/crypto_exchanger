package medvedev.com.repository;

import medvedev.com.entity.SystemConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfigurationEntity, Short> {

    Optional<SystemConfigurationEntity> findByName(String name);

    @Modifying
    @Query(value = "UPDATE SystemConfigurationEntity sc SET sc.value = :value WHERE sc.name = :name")
    void setConfigurationByName(@Param("value") String value, @Param("name") String name);
}
