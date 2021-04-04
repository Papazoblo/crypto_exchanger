package medvedev.com.service.telegram.handler;

import medvedev.com.service.telegram.MessageSenderService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class AuthorizationHandler extends BaseHandlerHandlerImpl {

    public AuthorizationHandler(MessageSenderService senderService) {
        super(senderService);
    }

    @Override
    public void run(Message message) {
        sendMessage("Okey! It's fine", message.getChatId());
    }
}
