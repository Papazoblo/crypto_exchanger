package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.dto.property.ConfigurationProperty;
import medvedev.com.enums.PriceChangeState;
import medvedev.com.service.validator.BuyValidator;
import medvedev.com.service.validator.Validator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ExchangeConfigDecryptorService {

    private static final int CATEGORY_SIZE = 1;
    private static final String DEC = "d";
    private static final String INC = "i";
    private static final String SPLITERATOR = ",";
    private static final String SPLITERATOR_GROUP = "/";

    private final ConfigurationProperty property;

    public List<PriceChangeState[]> getConfig(Validator validator) {
        return validator instanceof BuyValidator ? getBuyConfig() : getSellConfig();
    }

    public List<PriceChangeState[]> getBuyConfig() {
        return decryptConfigString(property.getBuy());
    }

    public List<PriceChangeState[]> getSellConfig() {
        return decryptConfigString(property.getSell());
    }

    private static List<PriceChangeState[]> decryptConfigString(String config) {
        String[] splitGroupConfig = config.toLowerCase().split(SPLITERATOR_GROUP);
        return Arrays.stream(splitGroupConfig).map(group -> {
            String[] splitConfig = group.split(SPLITERATOR);
            return Arrays.stream(splitConfig).flatMap(item -> {
                int count = Integer.parseInt(item.substring(CATEGORY_SIZE));
                if (item.contains(DEC)) {
                    return convertToSchema(count, PriceChangeState.DECREASED).stream();
                } else if (item.contains(INC)) {
                    return convertToSchema(count, PriceChangeState.INCREASED).stream();
                }
                return Stream.empty();
            }).toArray(PriceChangeState[]::new);
        }).collect(toList());
    }

    private static List<PriceChangeState> convertToSchema(int count, PriceChangeState state) {
        List<PriceChangeState> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(state);
        }
        return list;
    }
}
