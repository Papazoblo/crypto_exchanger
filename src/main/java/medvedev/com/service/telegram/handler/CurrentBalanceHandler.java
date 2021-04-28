package medvedev.com.service.telegram.handler;

import com.binance.api.client.domain.account.Account;
import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.enums.Currency;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class CurrentBalanceHandler extends BaseHandlerHandlerImpl {

    private final BinanceClient binanceClient;

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
        Account account = binanceClient.getAccountInfo();
        messageSender.accept(buildMessage(account), message.getChatId());
    }

    private static String buildMessage(Account account) {
        return new StringBuilder("*Balance info*\n_")
                .append(Currency.ETH.name())
                .append("_: ")
                .append(account.getAssetBalance(Currency.ETH.name()).getFree())
                .append("\n_")
                .append(Currency.USDT.name())
                .append("_: ")
                .append(account.getAssetBalance(Currency.USDT.name()).getFree())
                .toString();
    }
}
