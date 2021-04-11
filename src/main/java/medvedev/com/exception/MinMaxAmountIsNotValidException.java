package medvedev.com.exception;

import java.math.BigDecimal;

public class MinMaxAmountIsNotValidException extends RuntimeException {

    public MinMaxAmountIsNotValidException(BigDecimal minAmount, BigDecimal maxAmount) {
        super(String.format("Min/max amounts not valid [%d, %d]", minAmount.intValue(), maxAmount.intValue()));
    }
}
