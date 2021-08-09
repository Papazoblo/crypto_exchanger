package medvedev.com.service.telegram.handler;

import medvedev.com.enums.SystemConfiguration;
import medvedev.com.service.SystemConfigurationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE;
import static medvedev.com.enums.SystemConfiguration.MIN_DIFFERENCE_PRICE_FIAT_CRYPT;

@Service
public class ChangePricePercentDifferenceHandler extends BaseHandlerHandlerImpl {

    private static final int CONFIGURATION_VALUE = 1;

    private final SystemConfigurationService systemConfigurationService;

    public ChangePricePercentDifferenceHandler(SystemConfigurationService systemConfigurationService) {
        this.systemConfigurationService = systemConfigurationService;
    }

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {

        String messageText = message.getText();
        SystemConfiguration configuration = messageText.contains("cryptfiat") ? MIN_DIFFERENCE_PRICE
                : MIN_DIFFERENCE_PRICE_FIAT_CRYPT;
        messageSender.accept(saveValue(messageText, configuration), message.getChatId());
    }

    private String saveValue(String message, SystemConfiguration configuration) {
        String[] splitMessage = message.split(" ");
        String response;

        if (splitMessage.length == 2) {
            try {
                Double.valueOf(splitMessage[CONFIGURATION_VALUE]);
                systemConfigurationService.setConfigurationByName(configuration, splitMessage[CONFIGURATION_VALUE]);
                response = "Value success changed";
            } catch (NumberFormatException ex) {
                response = "Value is not a number";
            }
        } else {
            response = configuration.getName() + " value: " + systemConfigurationService.findBdByName(configuration);
        }
        return response;
    }
}
