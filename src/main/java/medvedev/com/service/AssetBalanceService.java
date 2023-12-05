package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.client.BinanceApiClient;
import medvedev.com.component.TimestampComponent;
import medvedev.com.dto.property.BinanceProperty;
import medvedev.com.dto.response.BalanceInfoResponse;
import medvedev.com.entity.AssetBalanceEntity;
import medvedev.com.enums.Currency;
import medvedev.com.repository.AssetBalanceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetBalanceService {

    private final AssetBalanceRepository repository;
    private final BinanceApiClient binanceApiClient;
    private final TimestampComponent timestampComponent;
    private final BinanceProperty binanceProperty;

    public void create() {

        BalanceInfoResponse balanceUsdt = binanceApiClient.getBalanceInfo(Currency.USDT.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);

        BalanceInfoResponse balanceEth = binanceApiClient.getBalanceInfo(Currency.ETH.name(),
                timestampComponent.getTimestamp(),
                binanceProperty.getRectWindow()).get(0);

        AssetBalanceEntity entity = new AssetBalanceEntity();
        entity.setUsdt(balanceUsdt.getFree().length() > 10 ? balanceUsdt.getFree().substring(0, 9) : balanceUsdt.getFree());
        entity.setEth(balanceEth.getFree().length() > 10 ? balanceEth.getFree().substring(0, 9) : balanceEth.getFree());
        repository.save(entity);
    }
}
