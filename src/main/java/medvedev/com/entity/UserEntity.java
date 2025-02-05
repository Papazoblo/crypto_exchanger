package medvedev.com.entity;

import lombok.Data;

import javax.persistence.*;

@Table(schema = "cr_schema", name = "users")
@Entity
@Data
public class UserEntity {

    @Id
    @SequenceGenerator(schema = "cr_schema", sequenceName = "users_id_seq",
            name = "users_id_seq_GEN", allocationSize = 1)
    @GeneratedValue(generator = "users_id_seq_GEN", strategy = GenerationType.SEQUENCE)
    private Short id;

    @Column
    private String login;

    @Column
    private String password;
}
