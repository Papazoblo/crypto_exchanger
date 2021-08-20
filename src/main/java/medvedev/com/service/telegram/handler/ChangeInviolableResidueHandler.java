package medvedev.com.service.telegram.handler;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.SystemConfigurationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
public class ChangeInviolableResidueHandler implements BaseHandler {

    private final SystemConfigurationService systemConfigurationService;

    public ChangeInviolableResidueHandler(SystemConfigurationService systemConfigurationService) {
        this.systemConfigurationService = systemConfigurationService;
    }

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        String messageText = message.getText();
        messageSender.accept(processCommand(messageText), message.getChatId());
    }

    private String processCommand(String command) {
        String[] splitMessage = command.split(" ");
        String response;

        switch (splitMessage.length) {
            case UPDATE_COMMAND_LENGTH:
                response = saveValue(splitMessage[CONFIGURATION_VALUE]);
                break;
            case GET_COMMAND_LENGTH:
                response = systemConfigurationService.findByName(SystemConfiguration.INVIOLABLE_RESIDUE);
                break;
            default:
                response = "You entered invalid command";
        }
        return response;
    }

    private String saveValue(String value) {
        try {
            Double.valueOf(value);
            systemConfigurationService.setConfigurationByName(SystemConfiguration.INVIOLABLE_RESIDUE, value);
            return "Value success changed";
        } catch (NumberFormatException ex) {
            return "Value is not a number";
        }
    }
}
