package medvedev.com.service.telegram.handler;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.SystemConfigurationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE;

@Service
public class ChangePricePercentDifferenceHandler implements BaseHandler {

    private final SystemConfigurationService systemConfigurationService;

    public ChangePricePercentDifferenceHandler(SystemConfigurationService systemConfigurationService) {
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
        SystemConfiguration configuration = MIN_DIFFERENCE_PRICE;
        switch (splitMessage.length) {
            case UPDATE_COMMAND_LENGTH:
                response = saveValue(splitMessage[CONFIGURATION_VALUE], configuration);
                break;
            case GET_COMMAND_LENGTH:
                response = systemConfigurationService.findByName(configuration);
                break;
            default:
                response = "You entered invalid command";
        }
        return response;
    }

    private String saveValue(String value, SystemConfiguration configuration) {
        try {
            Double.valueOf(value);
            systemConfigurationService.setConfigurationByName(configuration, value);
            return "Value success changed";
        } catch (NumberFormatException ex) {
            return "Value is not a number";
        }
    }
}
