package medvedev.com.repository;

import medvedev.com.entity.ChatStateEntity;
import medvedev.com.enums.ChatState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatStateRepository extends JpaRepository<ChatStateEntity, Long> {

    Optional<ChatStateEntity> findByIdChat(Long idChat);

    @Query(value = "SELECT cs.idChat FROM ChatStateEntity cs WHERE cs.state = :state")
    List<Long> findAllByState(@Param("state") ChatState state);

    @Query(value = "UPDATE ChatStateEntity cs SET cs.state = :state " +
            "WHERE cs.idChat = :idChat")
    @Modifying
    void changeStateByIdChat(@Param("idChat") Long idChat,
                             @Param("state") ChatState stateName);
}
