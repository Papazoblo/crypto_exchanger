package medvedev.com.service.telegram.handler;

import medvedev.com.service.telegram.MessageSenderService;

public abstract class BaseHandlerHandlerImpl implements BaseHandler {

    private final MessageSenderService senderService;

    public BaseHandlerHandlerImpl(MessageSenderService senderService) {
        this.senderService = senderService;
    }

    protected void sendMessage(String message, Long chatId) {
        senderService.sendMessage(message, chatId);
    }
}
