package medvedev.com.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Log4j2
public class TelegramConnector {

    private final TelegramPollingService pollingService;

    @PostConstruct
    public void connect() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(TelegramBotSession.class);
            telegramBotsApi.registerBot(pollingService);
        } catch (TelegramApiException ex) {
            log.debug(ex);
        }
    }
}
