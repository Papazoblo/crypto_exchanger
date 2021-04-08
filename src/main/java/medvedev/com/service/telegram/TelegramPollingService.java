package medvedev.com.service.telegram;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.property.TelegramProperty;
import medvedev.com.service.telegram.handler.BaseHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Service
public class TelegramPollingService extends TelegramLongPollingBot {

    private static final Logger log = Logger.getLogger(String.valueOf(TelegramPollingService.class));

    private final TelegramProperty properties;
    private final TelegramMessageParserService parserService;

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
        BaseHandler handler = parserService.parseMessage(message);
        handler.run(message, this::sendMessage);
        //TODO добавить обработку сообщений
        log.info(updates.toString());
    }

    private void sendMessage(String message, Long idChat) {
        String chat = String.valueOf(idChat);
        BotApiMethod<Message> method = new SendMessage(chat, message);
        try {
            execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
