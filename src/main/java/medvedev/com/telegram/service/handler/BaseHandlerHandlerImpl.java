package medvedev.com.telegram.service.handler;

import medvedev.com.telegram.service.MessageSenderService;

public abstract class BaseHandlerHandlerImpl implements BaseHandler {

    private final MessageSenderService senderService;

    public BaseHandlerHandlerImpl(MessageSenderService senderService) {
        this.senderService = senderService;
    }

    protected void sendMessage(String message, Long chatId) {
        senderService.sendMessage(message, chatId);
    }
}
