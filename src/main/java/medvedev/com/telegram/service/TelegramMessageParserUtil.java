package medvedev.com.telegram.service;

import lombok.experimental.UtilityClass;
import medvedev.com.telegram.service.handler.AuthorizationHandler;
import medvedev.com.telegram.service.handler.BaseHandler;
import medvedev.com.telegram.service.handler.IncorrectCommandHandler;
import org.telegram.telegrambots.meta.api.objects.Message;

@UtilityClass
public class TelegramMessageParserUtil {

    private static final String BOT_NAME_DELIMITER = "@";

    public static BaseHandler parseMessage(Message message, MessageSenderService senderService) {
        String command = getCommandWithoutBotName(message.getText());
        switch (command) {
            case "/start":
                return new AuthorizationHandler(senderService);
            default:
                return new IncorrectCommandHandler(senderService);
        }
    }

    private static String getCommandWithoutBotName(String command) {
        return command.split(BOT_NAME_DELIMITER)[0].trim();
    }
}
