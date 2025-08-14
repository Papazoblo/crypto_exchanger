package medvedev.com.service.telegram.handler;

import lombok.RequiredArgsConstructor;
import medvedev.com.service.BinanceClientService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class CurrentPriceHandler implements BaseHandler {

    private final BinanceClientService binanceClientService;

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        BigDecimalWrapper lastPrice = binanceClientService.getCurrentPrice();
        StringBuilder sb = new StringBuilder("*Last price*: ");
        sb.append(lastPrice);
//        sb.append("\n*Price to exchange*: ");
//        sb.append(priceDifferenceService.getPriceToExchange());
        messageSender.accept(sb.toString(), message.getChatId());
    }
}
