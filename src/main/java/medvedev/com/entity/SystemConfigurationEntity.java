package medvedev.com.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "system_configuration")
@Entity
@Data
public class SystemConfigurationEntity {

    @Id
    private String name;

    @Column
    private String value;
}
