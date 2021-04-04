package medvedev.com.telegram.service.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BaseHandler {

    void run(Message message);
}
