package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.SystemConfigurationDto;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.enums.SystemState;
import medvedev.com.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import static medvedev.com.enums.SystemState.LAUNCHED;

@RequiredArgsConstructor
@Service
public class SystemStateService {

    private final SystemConfigurationService systemConfigurationService;

    public boolean isSystemNotLaunched() {
        try {
            SystemState state = SystemState.valueOf(
                    systemConfigurationService.findByName(SystemConfiguration.SYSTEM_STATE));
            return state != LAUNCHED;
        } catch (EntityNotFoundException ex) {
            systemConfigurationService.save(createSystemStateConfiguration());
            return true;
        }
    }

    private static SystemConfigurationDto createSystemStateConfiguration() {
        return new SystemConfigurationDto(
                SystemConfiguration.SYSTEM_STATE.name(),
                LAUNCHED.name()
        );
    }
}
