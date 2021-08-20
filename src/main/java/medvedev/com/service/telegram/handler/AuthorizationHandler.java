package medvedev.com.service.telegram.handler;

import medvedev.com.enums.ChatState;
import medvedev.com.service.security.ChatStateService;
import medvedev.com.service.security.UserService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
public class AuthorizationHandler implements BaseHandler {

    private static final int USER_LOGIN = 1;
    private static final int USER_PASSWORD = 2;
    private static final int VALID_LENGTH_SPLIT_COMMAND = 3;

    private final UserService userService;
    private final ChatStateService chatStateService;

    public AuthorizationHandler(UserService userService, ChatStateService chatStateService) {
        this.userService = userService;
        this.chatStateService = chatStateService;
    }

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        if (isChatAlreadyAuthenticated(message.getChatId())) {
            messageSender.accept("This chat already authenticated.", message.getChatId());
            return;
        }

        if (isUserExist(message.getText())) {
            chatStateService.updateChatState(message.getChatId(), ChatState.AUTHENTICATED);
            messageSender.accept("You are successfully logged in", message.getChatId());
        } else {
            messageSender.accept("This user not exist", message.getChatId());
        }
    }

    private boolean isChatAlreadyAuthenticated(Long idChat) {
        return chatStateService.getStateByChat(idChat) == ChatState.AUTHENTICATED;
    }

    private boolean isUserExist(String line) {
        String[] splitLine = line.split(" ");
        if (splitLine.length != VALID_LENGTH_SPLIT_COMMAND) {
            return false;
        }
        return userService.isUserAuthenticated(splitLine[USER_LOGIN], splitLine[USER_PASSWORD]);
    }
}
