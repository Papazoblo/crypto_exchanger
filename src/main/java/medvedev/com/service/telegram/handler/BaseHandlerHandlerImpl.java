package medvedev.com.service.telegram.handler;

import medvedev.com.service.security.ChatStateService;

public abstract class BaseHandlerHandlerImpl implements BaseHandler {

    protected final ChatStateService chatStateService;

    public BaseHandlerHandlerImpl(ChatStateService chatStateService) {
        this.chatStateService = chatStateService;
    }

}
