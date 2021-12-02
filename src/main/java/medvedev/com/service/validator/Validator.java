package medvedev.com.service.validator;

import medvedev.com.dto.PriceHistoryDto;
import medvedev.com.enums.PriceChangeState;

import java.util.List;

public interface Validator {

    default boolean validate(PriceHistoryDto[] history, List<PriceChangeState[]> configs) {
        boolean isValid;
        for (PriceChangeState[] config : configs) {
            isValid = true;
            for (int i = 0; i < config.length - 1; i++) {
                if (history[i] == null || history[i].getChangeState() != config[i]) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                return true;
            }
        }
        return false;
    }
}
