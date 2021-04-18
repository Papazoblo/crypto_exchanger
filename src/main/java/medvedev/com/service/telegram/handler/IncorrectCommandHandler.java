package medvedev.com.service.telegram.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class IncorrectCommandHandler extends BaseHandlerHandlerImpl {

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        messageSender.accept("I don't understand you. Please try again.", message.getChatId());
    }
}
