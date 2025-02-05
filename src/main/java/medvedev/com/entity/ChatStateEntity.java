package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.ChatState;

import javax.persistence.*;

@Table(schema = "cr_schema", name = "chat_id_state")
@Entity
@Data
public class ChatStateEntity {

    @Id
    @Column(name = "id_chat")
    private Long idChat;

    @Column
    @Enumerated(EnumType.STRING)
    private ChatState state;
}
