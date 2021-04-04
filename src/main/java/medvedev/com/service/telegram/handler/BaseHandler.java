package medvedev.com.service.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BaseHandler {

    void run(Message message);
}
