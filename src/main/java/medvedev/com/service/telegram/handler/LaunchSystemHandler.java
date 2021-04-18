package medvedev.com.service.telegram.handler;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.service.SystemConfigurationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

import static medvedev.com.enums.SystemState.LAUNCHED;

@Service
public class LaunchSystemHandler extends BaseHandlerHandlerImpl {

    private final SystemConfigurationService systemConfigurationService;

    public LaunchSystemHandler(SystemConfigurationService systemConfigurationService) {
        this.systemConfigurationService = systemConfigurationService;
    }

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        try {
            systemConfigurationService.setConfigurationByName(SystemConfiguration.SYSTEM_STATE, LAUNCHED.name());
            messageSender.accept("System successfully launched", message.getChatId());
        } catch (EntityNotFoundException ex) {
            //TODO log
            messageSender.accept("Error system launching", message.getChatId());
        }
    }
}
