package medvedev.com.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class MessageSenderService {

    private final TelegramPollingService pollingService;

    public void sendMessage(String messageText, Long chatId) {
        String chat = String.valueOf(chatId);
        BotApiMethod<Message> method = new SendMessage(chat, messageText);
        try {
            pollingService.execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
