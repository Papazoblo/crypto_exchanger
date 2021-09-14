package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.enums.SystemConfiguration;
import medvedev.com.exception.MinMaxAmountIsNotValidException;
import medvedev.com.exception.NotEnoughFundsBalanceException;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BalanceCheckerService {

    private static final int ROUND_ACCURACY = 0;

    private final SystemConfigurationService systemConfigurationService;

    public BigDecimalWrapper getAmountToExchange(String stringBalance) {

        BigDecimalWrapper balance = new BigDecimalWrapper(stringBalance);
        BigDecimalWrapper minAmount = systemConfigurationService.findBdByName(SystemConfiguration.MIN_AMOUNT_EXCHANGE);
        BigDecimalWrapper maxAmount = systemConfigurationService.findBdByName(SystemConfiguration.MAX_AMOUNT_EXCHANGE);
        validatedMinMaxAmount(minAmount, maxAmount);

        return isEnoughFundsBalance(balance, minAmount, maxAmount);
    }

    public BigDecimalWrapper isEnoughFundsBalance(BigDecimalWrapper balance, BigDecimalWrapper minAmount,
                                                  BigDecimalWrapper maxAmount) {

        if (balance.isLessThen(minAmount)) { // если баланс < минимума
            throw new NotEnoughFundsBalanceException(balance);
        } else if (balance.isLessThen(maxAmount)) { // если баланс < максимума
            return new BigDecimalWrapper(balance.setScale(ROUND_ACCURACY, RoundingMode.DOWN).toString());
        } else { // баланс больше максимума
            return maxAmount;
        }
    }

    private void validatedMinMaxAmount(BigDecimalWrapper minAmount, BigDecimalWrapper maxAmount) {
        if (minAmount.isGreaterThenOrEqual(maxAmount)) {
            throw new MinMaxAmountIsNotValidException(minAmount, maxAmount);
        }
    }
}
