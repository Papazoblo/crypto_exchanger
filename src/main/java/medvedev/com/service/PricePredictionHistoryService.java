package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import medvedev.com.entity.PricePredictionHistoryEntity;
import medvedev.com.exception.EntityNotFoundException;
import medvedev.com.repository.PricePredictionHistoryRepository;
import medvedev.com.wrapper.BigDecimalWrapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricePredictionHistoryService {

    private final PricePredictionHistoryRepository repository;
    private final TimeService timeService;

    public BigDecimalWrapper getLast() {

        String price = repository.findTopByDateIsBeforeOrderByDateDesc(timeService.now())
                .orElseThrow(() -> new EntityNotFoundException("price_prediction_history", "price"))
                .getPrice();

        return new BigDecimalWrapper(price);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void save(BigDecimalWrapper price) {
        repository.save(new PricePredictionHistoryEntity(
                timeService.now(),
                price.toString()
        ));
    }
}
