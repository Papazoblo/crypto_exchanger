package medvedev.com.telegram.service.handler;

import medvedev.com.telegram.service.MessageSenderService;
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
