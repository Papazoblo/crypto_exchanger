package medvedev.com.service.telegram.handler;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class CurrentPriceHandler extends BaseHandlerHandlerImpl {

    private final BinanceClient binanceClient;

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        String lastPrice = binanceClient.getPriceInfo().getLastPrice();
        messageSender.accept("*Last price*: " + lastPrice, message.getChatId());
    }
}
