package medvedev.com.service.exchangefactory;

import com.binance.api.client.domain.OrderSide;
import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceClient;
import medvedev.com.service.BalanceCheckerService;
import medvedev.com.service.ExchangeHistoryService;
import medvedev.com.service.SystemConfigurationService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ExchangeStrategyFactory {

    private final BinanceClient client;
    private final ExchangeHistoryService historyService;
    private final BalanceCheckerService balanceCheckerService;
    private final SystemConfigurationService systemConfigurationService;

    public ExchangeStrategy getStrategy(OrderSide side) {

        if (side == OrderSide.BUY) {
            return new FiatCryptExchangeStrategy(balanceCheckerService, client, historyService,
                    systemConfigurationService);
        }
        return new CryptFiatExchangeStrategy(client, historyService, systemConfigurationService);
    }
}
