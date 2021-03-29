package medvedev.com.repository;

import medvedev.com.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Short> {

    boolean existsByLoginAndPassword(String login, String password);
}
