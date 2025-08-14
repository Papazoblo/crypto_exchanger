package medvedev.com.wrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public BigDecimalWrapper(BigDecimal val) {
        super(val.toString());
    }

    public boolean isLessThen(BigDecimal val) {
        return compareTo(val) == LESS;
    }

    @Override
    public BigDecimalWrapper abs() {
        return new BigDecimalWrapper(super.abs());
    }

    @Override
    public BigDecimalWrapper subtract(BigDecimal subtrahend) {
        return new BigDecimalWrapper(super.subtract(subtrahend));
    }

    public BigDecimalWrapper setScale(int scale, RoundingMode roundingMode) {
        return new BigDecimalWrapper(super.setScale(scale, roundingMode));
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
        return new BigDecimalWrapper(val);
    }

    public boolean isGreaterThenOrEqual(BigDecimal val) {
        return isGreaterThen(val) || isEqual(val);
    }
}
