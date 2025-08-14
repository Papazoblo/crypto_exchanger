package medvedev.com.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BlockTimeType {

    MIN_3(3, true),
    MIN_5(5, true),
    MIN_10(10, true),
    MIN_15(15, true),
    MIN_30(30, true),
    HOUR_1(60, false),
    HOUR_2(120, false),
    HOUR_4(240, false),
    HOUR_8(480, false),
    HOUR_12(720, false);

    @Getter
    private final int minutes;
    @Getter
    private final boolean exclude;
}
