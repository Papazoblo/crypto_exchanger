package medvedev.com.telegram.service.handler;

import medvedev.com.telegram.service.MessageSenderService;
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
