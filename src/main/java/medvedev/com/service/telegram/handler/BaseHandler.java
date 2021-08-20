package medvedev.com.service.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

public interface BaseHandler {

    int CONFIGURATION_VALUE = 1;
    int GET_COMMAND_LENGTH = 1;
    int UPDATE_COMMAND_LENGTH = 2;

    void run(Message message, BiConsumer<String, Long> messageSender);
}
