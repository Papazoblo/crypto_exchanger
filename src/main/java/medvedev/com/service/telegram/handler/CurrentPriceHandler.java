package medvedev.com.service.telegram.handler;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.service.CheckPriceDifferenceService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class CurrentPriceHandler implements BaseHandler {

    private final BinanceApiClient binanceClient;
    private final CheckPriceDifferenceService priceDifferenceService;

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
//        String lastPrice = binanceClient.getPriceInfo().getLastPrice();
//        StringBuilder sb = new StringBuilder("*Last price*: ");
//        sb.append(lastPrice);
//        sb.append("\n*Price to exchange*: ");
//        sb.append(priceDifferenceService.getPriceToExchange());
//        messageSender.accept(sb.toString(), message.getChatId());
    }
}
