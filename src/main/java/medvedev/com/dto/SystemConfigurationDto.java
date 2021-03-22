package medvedev.com.dto;

import lombok.Data;
import medvedev.com.entity.SystemConfigurationEntity;
import org.springframework.lang.NonNull;

@Data
public class SystemConfigurationDto {

    @NonNull
    private String name;
    @NonNull
    private String value;

    public static SystemConfigurationDto from(SystemConfigurationEntity entity) {
        return new SystemConfigurationDto(
                entity.getName(),
                entity.getValue()
        );
    }

    public static SystemConfigurationEntity to(SystemConfigurationDto dto) {
        SystemConfigurationEntity entity = new SystemConfigurationEntity();
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        return entity;
    }
}
