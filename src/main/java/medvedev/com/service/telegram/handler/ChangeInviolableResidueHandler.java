package medvedev.com.service.telegram.handler;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.SystemConfigurationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
public class ChangeInviolableResidueHandler extends BaseHandlerHandlerImpl {

    private static final int CONFIGURATION_VALUE = 1;

    private final SystemConfigurationService systemConfigurationService;

    public ChangeInviolableResidueHandler(SystemConfigurationService systemConfigurationService) {
        this.systemConfigurationService = systemConfigurationService;
    }

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {

        String messageText = message.getText();
        messageSender.accept(saveValue(messageText), message.getChatId());
    }

    private String saveValue(String message) {
        String[] splitMessage = message.split(" ");
        String response;

        if (splitMessage.length == 2) {
            try {
                Double.valueOf(splitMessage[CONFIGURATION_VALUE]);
                systemConfigurationService.setConfigurationByName(SystemConfiguration.INVIOLABLE_RESIDUE,
                        splitMessage[CONFIGURATION_VALUE]);
                response = "Value success changed";
            } catch (NumberFormatException ex) {
                response = "Value is not a number";
            }
        } else {
            response = "You entered invalid command";
        }
        return response;
    }
}
