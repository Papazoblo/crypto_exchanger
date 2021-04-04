package medvedev.com.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class TelegramConnector {

    private final TelegramPollingService pollingService;

    @PostConstruct
    public void connect() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(TelegramBotSession.class);
            BotSession session = telegramBotsApi.registerBot(pollingService);
        } catch (TelegramApiException ex) {
            //TODO добавить лог
        }
    }
}
