package medvedev.com.service;

import medvedev.com.dto.SystemConfigurationDto;
import medvedev.com.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static medvedev.com.enums.SystemConfiguration.SYSTEM_STATE;
import static medvedev.com.enums.SystemState.STOPPED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SystemStateServiceTest {

    private SystemConfigurationService configurationService;
    private SystemStateService service;

    @BeforeEach
    void setUp() {
        configurationService = mock(SystemConfigurationService.class);
        service = new SystemStateService(configurationService);
    }

    @Nested
    class CheckSystemState {

        @Test
        void shouldReturnTrue() {

            when(configurationService.findByName(SYSTEM_STATE))
                    .thenReturn("STOPPED");

            assertTrue(service.isSystemNotLaunched());
            verify(configurationService, never()).save(any());
        }

        @Nested
        class ReturnFalse {

            @Test
            void shouldReturnFalseWhenException() {

                SystemConfigurationDto dto = new SystemConfigurationDto(
                        SYSTEM_STATE.name(), STOPPED.name());

                when(configurationService.findByName(SYSTEM_STATE))
                        .thenThrow(EntityNotFoundException.class);

                assertTrue(service.isSystemNotLaunched());
                verify(configurationService).save(dto);
            }

            @Test
            void shouldReturnFalse() {

                when(configurationService.findByName(SYSTEM_STATE))
                        .thenReturn("LAUNCHED");

                assertFalse(service.isSystemNotLaunched());
                verify(configurationService, never()).save(any());
            }
        }
    }
}
