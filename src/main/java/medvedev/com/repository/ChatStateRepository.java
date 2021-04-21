package medvedev.com.repository;

import medvedev.com.entity.ChatStateEntity;
import medvedev.com.enums.ChatState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatStateRepository extends JpaRepository<ChatStateEntity, Long> {

    Optional<ChatStateEntity> findByIdChat(Long idChat);

    @Query(value = "SELECT cs.idChat FROM ChatStateEntity cs WHERE cs.state.state = :state")
    List<ChatStateEntity> findAllByState(@Param("state") ChatState state);

    @Query(value = "UPDATE chat_id_state SET id_state = (SELECT id" +
            " FROM states WHERE name = :stateName) WHERE id_chat = :idChat", nativeQuery = true)
    @Modifying
    void changeStateByIdChat(@Param("idChat") Long idChat,
                             @Param("stateName") String stateName);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO chat_id_state (id_chat, id_state) VALUES (:idChat, (SELECT id" +
            " FROM states WHERE name = :stateName))", nativeQuery = true)
    void insertNewChatState(@Param("idChat") Long idChat,
                            @Param("stateName") String stateName);
}
