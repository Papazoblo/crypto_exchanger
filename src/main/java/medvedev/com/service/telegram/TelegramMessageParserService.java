package medvedev.com.service.telegram;

import lombok.RequiredArgsConstructor;
import medvedev.com.service.security.ChatStateService;
import medvedev.com.service.security.UserService;
import medvedev.com.service.telegram.handler.AuthorizationHandler;
import medvedev.com.service.telegram.handler.BaseHandler;
import medvedev.com.service.telegram.handler.IncorrectCommandHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class TelegramMessageParserService {

    private static final String BOT_NAME_DELIMITER = "@";

    private final UserService userService;
    private final ChatStateService chatStateService;

    public BaseHandler parseMessage(Message message) {
        String commandLine = getCommandWithoutBotName(message.getText());
        if (isCommandValid(commandLine)) {
            String command = getCommandName(commandLine);
            switch (command) {
                case "/start":
                    return new AuthorizationHandler(userService, chatStateService);
                default:
                    return new IncorrectCommandHandler(chatStateService);
            }
        } else {
            return new IncorrectCommandHandler(chatStateService);
        }
    }

    private static String getCommandWithoutBotName(String command) {
        return command.split(BOT_NAME_DELIMITER)[0].trim();
    }

    private static boolean isCommandValid(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            return false;
        }
        return line.startsWith("/");
    }

    private static String getCommandName(String line) {
        String[] array = line.split(" ");
        if (array.length != 0) {
            return array[0];
        } else {
            return "";
        }
    }
}
