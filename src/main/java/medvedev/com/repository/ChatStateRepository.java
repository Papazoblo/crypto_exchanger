package medvedev.com.repository;

import medvedev.com.entity.ChatStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatStateRepository extends JpaRepository<ChatStateEntity, Long> {

    Optional<ChatStateEntity> findByIdChat(Long idChat);

    @Query(value = "UPDATE chat_id_state SET id_state = (SELECT id" +
            " FROM states WHERE name = :stateName) WHERE id_chat = :idChat", nativeQuery = true)
    @Modifying
    void changeStateByIdChat(@Param("idChat") Long idChat,
                             @Param("stateName") String stateName);

    @Query(value = "INSERT INTO chat_id_state (id_chat, id_state) VALUES (:idChat, SELECT id" +
            " FROM states WHERE name = :stateName)", nativeQuery = true)
    void insertNewChatState(@Param("idChat") Long idChat,
                            @Param("stateName") String stateName);
}
