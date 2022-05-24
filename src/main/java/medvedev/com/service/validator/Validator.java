package medvedev.com.service.validator;

import medvedev.com.dto.PriceHistoryBlockDto;
import medvedev.com.enums.PriceChangeState;

import java.util.List;
import java.util.Optional;


public interface Validator {

    default Optional<PriceChangeState[]> validate(List<PriceHistoryBlockDto> history,
                                                  List<PriceChangeState[]> configs) {
        boolean isValid;
        for (PriceChangeState[] config : configs) {
            isValid = true;
            for (int i = 0; i < config.length - 1; i++) {
                if (history.get(config.length - 1 - i).getAvgChangeType() != config[i]) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                return Optional.of(config);
            }
        }
        return Optional.empty();
    }
}
