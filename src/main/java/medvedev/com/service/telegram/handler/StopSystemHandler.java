package medvedev.com.service.telegram.handler;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.security.ChatStateService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

import static medvedev.com.enums.SystemState.STOPPED;

@Service
public class StopSystemHandler extends BaseHandlerHandlerImpl {

    private final SystemConfigurationService systemConfigurationService;

    public StopSystemHandler(SystemConfigurationService systemConfigurationService,
                             ChatStateService chatStateService) {
        super(chatStateService);
        this.systemConfigurationService = systemConfigurationService;
    }

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {

        try {
            systemConfigurationService.setConfigurationByName(SystemConfiguration.SYSTEM_STATE, STOPPED.name());
            messageSender.accept("System successfully stopped", message.getChatId());
        } catch (EntityNotFoundException ex) {
            messageSender.accept("Error system stopping", message.getChatId());
        }
    }
}
