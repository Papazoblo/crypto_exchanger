package medvedev.com.service.telegram.handler;

import medvedev.com.service.telegram.MessageSenderService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class IncorrectCommandHandler extends BaseHandlerHandlerImpl {

    public IncorrectCommandHandler(MessageSenderService senderService) {
        super(senderService);
    }

    @Override
    public void run(Message message) {
        sendMessage("I don't understand you bitch", message.getChatId());
    }
}
