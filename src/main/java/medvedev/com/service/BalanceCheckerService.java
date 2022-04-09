package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.exception.NotEnoughFundsBalanceException;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BalanceCheckerService {

    private static final int ROUND_ACCURACY = 0;
    private static final BigDecimalWrapper minAmount = new BigDecimalWrapper(5);
    private static final BigDecimalWrapper maxAmount = new BigDecimalWrapper(1000);

    private final SystemConfigurationService systemConfigurationService;

    public BigDecimalWrapper getAmountToExchange(String stringBalance) {

        BigDecimalWrapper balance = new BigDecimalWrapper(stringBalance);
        if (balance.isLessThen(minAmount)) { // если баланс < минимума
            throw new NotEnoughFundsBalanceException(balance);
        } else if (balance.isLessThen(maxAmount)) { // если баланс < максимума
            return new BigDecimalWrapper(balance.setScale(ROUND_ACCURACY, RoundingMode.DOWN).toString());
        } else { // баланс больше максимума
            return maxAmount;
        }
    }
}
