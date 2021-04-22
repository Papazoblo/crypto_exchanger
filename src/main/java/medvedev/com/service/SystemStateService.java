package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.SystemConfigurationDto;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.enums.SystemState;
import medvedev.com.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import static medvedev.com.enums.SystemState.LAUNCHED;
import static medvedev.com.enums.SystemState.STOPPED;

@RequiredArgsConstructor
@Service
@Log4j2
public class SystemStateService {

    private final SystemConfigurationService systemConfigurationService;

    public boolean isSystemNotLaunched() {
        try {
            SystemState state = SystemState.valueOf(
                    systemConfigurationService.findByName(SystemConfiguration.SYSTEM_STATE));
            return state != LAUNCHED;
        } catch (EntityNotFoundException ex) {
            log.debug(ex);
            systemConfigurationService.save(createSystemStateConfiguration());
            return true;
        }
    }

    private static SystemConfigurationDto createSystemStateConfiguration() {
        return new SystemConfigurationDto(
                SystemConfiguration.SYSTEM_STATE.name(),
                STOPPED.name()
        );
    }
}
