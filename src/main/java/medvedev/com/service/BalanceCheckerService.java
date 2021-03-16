package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.NotEnoughFundsBalanceException;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BalanceCheckerService {

    private final int ROUND_ACCURACY = 0;

    private final SystemConfigurationService systemConfigurationService;

    public BigDecimalWrapper isEnoughFundsBalance(String balance) {
        return isEnoughFundsBalance(new BigDecimalWrapper(balance));
    }

    public BigDecimalWrapper isEnoughFundsBalance(BigDecimalWrapper balance) {
        BigDecimalWrapper minAmount = systemConfigurationService.findBdByName(SystemConfiguration.MIN_AMOUNT_EXCHANGE);
        BigDecimalWrapper maxAmount = systemConfigurationService.findBdByName(SystemConfiguration.MAX_AMOUNT_EXCHANGE);

        if (balance.isLessThen(minAmount)) { // если баланс < минимума
            throw new NotEnoughFundsBalanceException(balance);
        } else if (balance.isLessThen(maxAmount)) { // если баланс < максимума
            return (BigDecimalWrapper) balance.setScale(ROUND_ACCURACY, RoundingMode.DOWN);
        } else { // баланс больше максимума
            return maxAmount;
        }
    }
}
