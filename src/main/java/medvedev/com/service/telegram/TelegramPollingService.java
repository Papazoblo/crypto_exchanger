package medvedev.com.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import medvedev.com.dto.property.TelegramProperty;
import medvedev.com.service.security.ChatStateService;
import medvedev.com.service.telegram.handler.BaseHandler;
import medvedev.com.utils.CommandListBuilder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.List;

import static medvedev.com.utils.StringUtils.transformTgMessage;

@RequiredArgsConstructor
@Service
@Log4j2
public class TelegramPollingService extends TelegramLongPollingBot {

    private final TelegramProperty properties;
    private final TelegramMessageParserService parserService;
    private final ChatStateService chatStateService;

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
        setCommandList();
    }

    @Override
    public void onUpdateReceived(Update update) {
        onUpdatesReceived(Collections.singletonList(update));
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        Message message = updates.get(0).getMessage();
        BaseHandler handler = parserService.parseMessage(message);
        handler.run(message, this::sendMessage);
    }

    public void sendMessage(String message) {
        chatStateService.getAuthenticatedChats().forEach(chat -> sendMessage(message, chat));
    }

    private void sendMessage(String message, Long idChat) {
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(idChat))
                .parseMode(ParseMode.MARKDOWN)
                .text(message)
                .build();
        executeCommand(method);
        log.info(transformTgMessage(message));
    }

    private void setCommandList() {
        SetMyCommands.SetMyCommandsBuilder commandsBuilder = SetMyCommands.builder();
        commandsBuilder.commands(CommandListBuilder.getCommandList());
        executeCommand(commandsBuilder.build());
    }

    private void executeCommand(BotApiMethod<?> method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.debug(e);
        }
    }
}
