package medvedev.com.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemConfiguration {

    SYSTEM_STATE("System state"),
    MIN_DIFFERENCE_PRICE("Min difference price"),
    CURRENT_PRICE_LEVEL("Current price level"),
    INVIOLABLE_RESIDUE("Inviolable residue"),
    AVAILABLE_MINUTES_COUNT_WITHOUT_EXCHANGE("Available minutes count without exchange");

    private final String name;
}
