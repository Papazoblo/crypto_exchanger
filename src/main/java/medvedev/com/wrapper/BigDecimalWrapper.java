package medvedev.com.wrapper;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalWrapper extends BigDecimal {

    public static final BigDecimalWrapper CONSTANT = new BigDecimalWrapper(100);
    private final int LESS = -1;
    private final int EQUALS = 0;
    private final int GREATER = 1;

    public BigDecimalWrapper(String val) {
        super(val);
    }

    public BigDecimalWrapper(int val) {
        super(val);
    }

    public BigDecimalWrapper(double val) {
        super(val);
    }

    public boolean isLessThen(BigDecimal val) {
        return compareTo(val) == LESS;
    }

    public boolean isGreaterThen(BigDecimal val) {
        return compareTo(val) == GREATER;
    }

    public boolean isEqual(BigDecimal val) {
        return compareTo(val) == EQUALS;
    }

    public boolean isLessThenOrEqual(BigDecimal val) {
        return isLessThen(val) || isEqual(val);
    }

    public static BigDecimalWrapper of(String val) {
        BigDecimal value = new BigDecimal(val).multiply(CONSTANT).round(MathContext.UNLIMITED);
        return new BigDecimalWrapper(value.toString());
    }

    public boolean isGreaterThenOrEqual(BigDecimal val) {
        return isGreaterThen(val) || isEqual(val);
    }
}
