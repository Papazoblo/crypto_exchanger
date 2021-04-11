package medvedev.com.service.telegram;

import lombok.RequiredArgsConstructor;
import medvedev.com.enums.ChatState;
import medvedev.com.service.SystemConfigurationService;
import medvedev.com.service.security.ChatStateService;
import medvedev.com.service.security.UserService;
import medvedev.com.service.telegram.handler.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class TelegramMessageParserService {

    private static final String BOT_NAME_DELIMITER = "@";

    private final UserService userService;
    private final ChatStateService chatStateService;
    private final SystemConfigurationService systemConfigurationService;

    public BaseHandler parseMessage(Message message) {
        String commandLine = getCommandWithoutBotName(message.getText());

        if (isAuthenticated(message.getChatId())) {
            return getHandlerForAuthenticated(commandLine);
        } else {
            return getHandlerForNotAuthenticated(commandLine);
        }
    }

    private BaseHandler getHandlerForAuthenticated(String commandLine) {
        String command = getCommandName(commandLine);
        BaseHandler handler;
        switch (command) {
            case "/launched":
                handler = new LaunchSystemHandler(systemConfigurationService, chatStateService);
                break;
            case "/stopped":
                handler = new StopSystemHandler(systemConfigurationService, chatStateService);
                break;
            default:
                handler = new IncorrectCommandHandler(chatStateService);
        }
        return handler;
    }

    private BaseHandler getHandlerForNotAuthenticated(String commandLine) {
        String command = getCommandName(commandLine);
        if (command.equals("/start")) {
            return new AuthorizationHandler(userService, chatStateService);
        } else {
            return new NotAuthenticatedCommandHandler(chatStateService);
        }
    }

    private boolean isAuthenticated(Long idChat) {
        return chatStateService.getStateByChat(idChat) == ChatState.AUTHENTICATED;
    }

    private static String getCommandWithoutBotName(String command) {
        return command.split(BOT_NAME_DELIMITER)[0].trim();
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
