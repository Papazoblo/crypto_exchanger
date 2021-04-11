package medvedev.com.exception;

import java.math.BigDecimal;

public class NotEnoughFundsBalanceException extends RuntimeException {

    public NotEnoughFundsBalanceException(BigDecimal balance) {
        super(String.format("Not enough funds on the balance. Balance = %d", balance.intValue()));
    }
}
