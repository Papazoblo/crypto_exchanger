package medvedev.com.entity;

import lombok.Data;

import javax.persistence.*;

@Table(name = "chat_id_state")
@Entity
@Data
public class ChatStateEntity {

    @Id
    @Column(name = "id_chat")
    private Long idChat;

    @ManyToOne
    @JoinColumn(name = "id_state")
    private StateEntity state;
}
