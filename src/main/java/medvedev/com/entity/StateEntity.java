package medvedev.com.entity;

import lombok.Data;
import medvedev.com.enums.ChatState;

import javax.persistence.*;

@Table(name = "states")
@Entity
@Data
public class StateEntity {

    @Id
    private Short id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private ChatState state;
}
