package medvedev.com.service.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

public interface BaseHandler {

    void run(Message message, BiConsumer<String, Long> messageSender);
}
