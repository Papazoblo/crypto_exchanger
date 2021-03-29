package medvedev.com.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "users")
@Entity
@Data
public class UserEntity {

    @Id
    private Short id;

    @Column
    private String login;

    @Column
    private String password;
}
