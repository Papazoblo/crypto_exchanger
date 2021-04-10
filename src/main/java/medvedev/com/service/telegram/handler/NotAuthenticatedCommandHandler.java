package medvedev.com.service.telegram.handler;

import medvedev.com.service.security.ChatStateService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
public class NotAuthenticatedCommandHandler extends BaseHandlerHandlerImpl {

    public NotAuthenticatedCommandHandler(ChatStateService chatStateService) {
        super(chatStateService);
    }

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        messageSender.accept("You must be authenticate. Please enter command '/start [login] [password]'",
                message.getChatId());
    }
}
