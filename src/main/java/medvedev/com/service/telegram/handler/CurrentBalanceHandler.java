package medvedev.com.service.telegram.handler;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.response.BalanceInfoResponse;
import medvedev.com.enums.Currency;
import medvedev.com.service.BinanceClientService;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class CurrentBalanceHandler implements BaseHandler {

    private final BinanceClientService binanceClientService;

    @Override
    public void run(Message message, BiConsumer<String, Long> messageSender) {

        BalanceInfoResponse balanceUsdt = binanceClientService.getBalance(Currency.USDT);
        BalanceInfoResponse balanceEth = binanceClientService.getBalance(Currency.ETH);

        messageSender.accept(buildMessage(balanceUsdt, balanceEth), message.getChatId());
    }

    private String buildMessage(BalanceInfoResponse usdt, BalanceInfoResponse eth) {
        return new StringBuilder("*Balance info*\n_")
                .append(Currency.ETH.name())
                .append("_: ")
                .append(eth.getFree())
                .append("\n_")
                .append(Currency.USDT.name())
                .append("_: ")
                .append(usdt.getFree())
                .append("\n\n")
                .append("_Total USD_: *")
                .append(getTotalUsd(usdt.getFree(), eth.getFree()))
                .append("*")
                .toString();
    }

    private String getTotalUsd(String usd, String eth) {
        BigDecimalWrapper price = new BigDecimalWrapper(binanceClientService.getCurrentPrice());
        BigDecimalWrapper usdNumber = new BigDecimalWrapper(usd);
        BigDecimalWrapper ethNumber = new BigDecimalWrapper(eth);

        return price.multiply(ethNumber).add(usdNumber).divide(BigDecimal.ONE, 2, RoundingMode.DOWN).toString();
    }
}
