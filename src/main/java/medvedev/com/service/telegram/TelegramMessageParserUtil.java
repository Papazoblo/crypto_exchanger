package medvedev.com.service.telegram;

import lombok.experimental.UtilityClass;
import medvedev.com.service.telegram.handler.AuthorizationHandler;
import medvedev.com.service.telegram.handler.BaseHandler;
import medvedev.com.service.telegram.handler.IncorrectCommandHandler;
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
