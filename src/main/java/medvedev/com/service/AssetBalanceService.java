package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.response.BalanceInfoResponse;
import medvedev.com.entity.AssetBalanceEntity;
import medvedev.com.enums.Currency;
import medvedev.com.repository.AssetBalanceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetBalanceService {

    private final AssetBalanceRepository repository;
    private final BinanceClientService binanceClientService;

    public void create() {

        BalanceInfoResponse balanceUsdt = binanceClientService.getBalance(Currency.USDT);
        BalanceInfoResponse balanceEth = binanceClientService.getBalance(Currency.ETH);

        AssetBalanceEntity entity = new AssetBalanceEntity();
        entity.setUsdt(balanceUsdt.getFree().length() > 10 ? balanceUsdt.getFree().substring(0, 9) : balanceUsdt.getFree());
        entity.setEth(balanceEth.getFree().length() > 10 ? balanceEth.getFree().substring(0, 9) : balanceEth.getFree());
        repository.save(entity);
    }
}
