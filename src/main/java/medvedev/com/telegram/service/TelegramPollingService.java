package medvedev.com.telegram.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.telegram.dto.property.TelegramProperty;
import medvedev.com.telegram.service.handler.BaseHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Service
public class TelegramPollingService extends TelegramLongPollingBot {

    private static final Logger log = Logger.getLogger(String.valueOf(TelegramPollingService.class));

    private final TelegramProperty properties;

    @Override
    public String getBotUsername() {
        return properties.getBotName();
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    @Override
    public void onRegister() {

    }

    @Override
    public void onUpdateReceived(Update update) {
        onUpdatesReceived(Collections.singletonList(update));
        //TODO добавить обработку сообщений
        log.info(update.toString());
        //log.info(commandType.name());
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        Message message = updates.get(0).getMessage();
        BaseHandler handler = TelegramMessageParserUtil.parseMessage(message, new MessageSenderService(this));
        handler.run(message);
        //TODO добавить обработку сообщений
        log.info(updates.toString());
    }
}
