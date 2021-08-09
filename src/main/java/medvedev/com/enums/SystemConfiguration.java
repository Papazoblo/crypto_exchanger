package medvedev.com.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemConfiguration {

    MIN_AMOUNT_EXCHANGE("Min amount exchange"),
    MAX_AMOUNT_EXCHANGE("Max amount exchange"),
    SYSTEM_STATE("System state"),
    MIN_DIFFERENCE_PRICE("Min difference price CRYPT FIAT"),
    MIN_DIFFERENCE_PRICE_FIAT_CRYPT("Min difference price FIAT CRYPT"),
    CURRENT_PRICE_LEVEL("Current price level"),
    INVIOLABLE_RESIDUE("Inviolable residue"),
    AVAILABLE_MINUTES_COUNT_WITHOUT_EXCHANGE("Available minutes count without exchange");

    private final String name;
}
