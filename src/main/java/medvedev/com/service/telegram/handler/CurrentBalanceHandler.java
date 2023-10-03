package medvedev.com.service.telegram.handler;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceApiClient;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class CurrentBalanceHandler implements BaseHandler {

    private final BinanceApiClient binanceClient;

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {
//        Account account = binanceClient.getAccountInfo();
//        messageSender.accept(buildMessage(account), message.getChatId());
    }

   /* private String buildMessage(Account account) {
        return new StringBuilder("*Balance info*\n_")
                .append(Currency.ETH.name())
                .append("_: ")
                .append(account.getAssetBalance(Currency.ETH.name()).getFree())
                .append("\n_")
                .append(Currency.USDT.name())
                .append("_: ")
                .append(account.getAssetBalance(Currency.USDT.name()).getFree())
                .append("\n\n")
                .append("_Total USD_: *")
                .append(getTotalUsd(account.getAssetBalance(Currency.USDT.name()).getFree(),
                        account.getAssetBalance(Currency.ETH.name()).getFree()))
                .append("*")
                .toString();
    }

    private String getTotalUsd(String usd, String eth) {
//        BigDecimalWrapper price = new BigDecimalWrapper(binanceClient.getPriceInfo().getLastPrice());
        BigDecimalWrapper usdNumber = new BigDecimalWrapper(usd);
        BigDecimalWrapper ethNumber = new BigDecimalWrapper(eth);

        return null;//price.multiply(ethNumber).add(usdNumber).divide(BigDecimal.ONE, 2, RoundingMode.DOWN).toString();
    }*/
}
